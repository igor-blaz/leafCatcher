package leafCatcher.handlers;

import jakarta.annotation.PostConstruct;
import leafCatcher.history.ActionType;
import leafCatcher.history.FSMRoute;
import leafCatcher.history.HistoryService;
import leafCatcher.model.Event;
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
    private final HistoryService historyService;
    private final EventStorage eventStorage;
    private static final Logger log = LoggerFactory.getLogger(FSMDispatcher.class);
    private final List<AbstractFsmHandler> handlers;

    private final Map<ActionType, FsmRoute> routes = new EnumMap<>(ActionType.class);
    private final MessageFactory messageFactory;

    @PostConstruct
    public void init() {
        for (AbstractFsmHandler handler : handlers) {
            scanBean(handler);
        }
    }

    private void scanBean(Object bean) {
        // пробегаемся по методам бина
        for (Method method : bean.getClass().getMethods()) {
            FSMRoute fsm = AnnotationUtils.findAnnotation(method, FSMRoute.class);
            if (fsm != null) {
                ActionType state = fsm.value();
                routes.put(state, new FsmRoute(bean, method));
            }
        }
    }

    public Object dispatch(ActionType state, Update update, Long chatId, Long userId) {
        FsmRoute route = routes.get(state);
        // если мы хотим пропустить старт
        boolean skipStart = historyService.isSkipStart(userId);
        boolean isStartCommand =
                (update.hasMessage()
                        && update.getMessage().hasText()
                        && "/start".equals(update.getMessage().getText()
                ));

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
            if (nextRoute == null) {
                return new SendMessage(chatId.toString(),
                        "Я не знаю, что делать в состоянии " + nextState + " 🤔");
            }

            log.info("Глобальная команда /start. Переход в {}", nextState);
            try {
                return nextRoute.method().invoke(nextRoute.bean(), update, chatId, userId);
            } catch (Exception e) {
                throw new RuntimeException("Ошибка при вызове хендлера для " + nextState, e);
            }
        }

        if (route == null) {
            return new SendMessage(chatId.toString(), "Я не знаю, что делать в этом состоянии 🤔");
        }

        try {
            return route.method().invoke(route.bean(), update, chatId, userId);
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при вызове FSM-хендлера для " + state, e);
        }
    }


    private record FsmRoute(Object bean, Method method) {
    }
}

