package leafCatcher.handlers;

import jakarta.annotation.PostConstruct;
import leafCatcher.history.ActionType;
import leafCatcher.history.FSMRoute;
import leafCatcher.history.HistoryService;
import leafCatcher.model.Event;
import leafCatcher.service.deleteStrategy.BotMessage;
import leafCatcher.service.deleteStrategy.DeleteStrategy;
import leafCatcher.service.messageFactory.MessageFactory;
import leafCatcher.storage.EventStorage;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.lang.reflect.Method;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class FSMDispatcher {

    private static final Logger log = LoggerFactory.getLogger(FSMDispatcher.class);

    private final HistoryService historyService;
    private final EventStorage eventStorage;
    private final List<AbstractFsmHandler> handlers;
    private final MessageFactory messageFactory; // если не нужен – можно убрать

    private final Map<ActionType, FsmRoute> routes = new EnumMap<>(ActionType.class);

    @PostConstruct
    public void init() {
        for (AbstractFsmHandler handler : handlers) {
            scanBean(handler);
        }
    }

    private void scanBean(Object bean) {
        for (Method method : bean.getClass().getMethods()) {
            FSMRoute fsm = AnnotationUtils.findAnnotation(method, FSMRoute.class);
            if (fsm != null) {
                ActionType state = fsm.value();
                routes.put(state, new FsmRoute(bean, method));
            }
        }
    }

    /**
     * Центральная точка входа FSM: ВСЕ хендлеры с @FSMRoute ДОЛЖНЫ возвращать BotMessage.
     */
    public BotMessage dispatch(ActionType state, Update update, Long chatId, Long userId) {
        log.info("dispatch {}", state);

        boolean skipStart = historyService.isSkipStart(userId);
        boolean isStartCommand =
                (update.hasMessage()
                        && update.getMessage().hasText()
                        && "/start".equals(update.getMessage().getText()));

        // Глобальная команда /start, принудительный переход в INTRO или ROOT_IS_ABSENCE_INFO
        if (isStartCommand && !skipStart) {
            log.info("Forced Start");
            historyService.reset(chatId, userId);
            historyService.setSkipStart(userId);

            Event root = eventStorage.getRootEvent();
            ActionType nextState;

            if (root == null) {
                log.info("Корневого события нет, переключаемся в ROOT_IS_ABSENCE_INFO");
                nextState = ActionType.ROOT_IS_ABSENCE_INFO;
                historyService.setAttemptsToExecute(userId, 2);
            } else {
                log.info("Нашли корневое событие: {}", root.getShortName());
                nextState = ActionType.INTRO;
                historyService.setCurrentEvent(chatId, root);
            }

            historyService.setState(chatId, nextState);

            FsmRoute nextRoute = routes.get(nextState);
            log.info("NextRoute {}", nextRoute);
            log.info("NextState {}", nextState);

            if (nextRoute == null) {
                return wrapSendMessage(
                        new SendMessage(chatId.toString(),
                                "Я не знаю, что делать в состоянии " + nextState + " 🤔"),
                        DeleteStrategy.NONE
                );
            }

            log.info("Глобальная команда /start. Переход в {}", nextState);
            try {
                Object result = nextRoute.method().invoke(nextRoute.bean(), update, chatId, userId);
                return castToBotMessage(result, nextState, chatId);
            } catch (Exception e) {
                throw new RuntimeException("Ошибка при вызове хендлера для " + nextState, e);
            }
        }

        // Обычный переход по текущему состоянию
        FsmRoute route = routes.get(state);
        if (route == null) {
            return wrapSendMessage(
                    new SendMessage(chatId.toString(), "Я не знаю, что делать в этом состоянии 🤔"),
                    DeleteStrategy.NONE
            );
        }

        try {
            Object result = route.method().invoke(route.bean(), update, chatId, userId);
            return castToBotMessage(result, state, chatId);
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при вызове FSM-хендлера для " + state, e);
        }
    }

    /**
     * Жёстко приводим результат к BotMessage.
     * Если хендлер вернул null или что-то ещё — это ошибка в хендлере.
     */
    private BotMessage castToBotMessage(Object result, ActionType state, Long chatId) {
        if (result == null) {
            // Здесь можно либо кинуть исключение, либо вернуть диагностическое сообщение.
            // Я делаю диагностическое сообщение, чтобы бот не падал в проде.
            log.error("Хендлер для {} вернул null. Все @FSMRoute должны возвращать BotMessage.", state);
            return wrapSendMessage(
                    new SendMessage(chatId.toString(),
                            "Внутренняя ошибка: хендлер для " + state + " не вернул сообщение."),
                    DeleteStrategy.NONE
            );
        }

        if (result instanceof BotMessage bm) {
            return bm;
        }

        if (result instanceof SendMessage sm) {
            log.warn("Хендлер для {} вернул SendMessage. Лучше вернуть BotMessage напрямую.", state);
            return new BotMessage(sm, DeleteStrategy.NONE);
        }

        // Совсем неожиданный тип — логируем и возвращаем диагностическое сообщение.
        log.error("Хендлер для {} вернул неожиданный тип: {}", state, result.getClass());
        return wrapSendMessage(
                new SendMessage(chatId.toString(),
                        "Внутренняя ошибка: неожиданный результат хендлера для " + state + "."),
                DeleteStrategy.NONE
        );
    }

    private BotMessage wrapSendMessage(SendMessage sendMessage, DeleteStrategy strategy) {
        return new BotMessage(sendMessage, strategy);
    }

    private record FsmRoute(Object bean, Method method) {
    }
}