package leafCatcher.service;

import leafCatcher.handlers.FSMDispatcher;
import leafCatcher.history.ActionType;
import leafCatcher.history.HistoryService;
import leafCatcher.model.Event;
import leafCatcher.storage.EventStorage;
import leafCatcher.utilityClasses.Commands;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class EventMainService {
    private final FSMDispatcher fsmDispatcher;
    private final HistoryService historyService;
    private final EventStorage eventStorage;
    @Value("${admin.secret.command.cleanNeo4j}")
    private String adminCleanDb;

    //Работает с текстами, которые написал пользователь
    public SendMessage makeMessageByText(Update update, Long chatId, Long userId) {
        ActionType actionType = historyService.getActualState(chatId);
        if (Commands.isStartCommand(update)) {
            historyService.setState(chatId, ActionType.START);
            actionType = ActionType.START;
        }
        log.info("actualState {}", actionType);
        if (actionType == null) {
            historyService.setState(chatId, ActionType.ROOT_IS_ABSENCE_INFO);
        }
        if (actionType == ActionType.ERROR) {
            actionType = isErrorHandler(update, chatId, userId);
        }
        return dispatch(actionType, update, chatId, userId);
    }

    /**
     * Обрабатывает callback-запросы от нажатия кнопок Telegram.
     * <p>
     * Поведение:
     * - если бот ждёт текст, а пришёл callback (и это не /start), возвращает сообщение, что кнопка неактуальна;
     * - если data совпадает с именем ActionType — делегирует выполнение в dispatch;
     * - если data — UUID события, загружает событие и выполняет GET_CHILD;
     * - иначе возвращает сообщение об неизвестном callback.
     *
     * @param update апдейт от Telegram
     * @param chatId идентификатор чата
     * @param userId идентификатор пользователя
     * @return сообщение для отправки пользователю
     */
    public SendMessage makeMessageByCallback(Update update, Long chatId, Long userId) {
        log.info("CALLBACK🔥");
        ActionType current = historyService.getActualState(chatId);

        if (isTextState(current) && !Commands.isStartCommand(update)) {
            return new SendMessage(chatId.toString(),
                    "Эта кнопка уже неактуальна 🙂\nСейчас я жду от тебя текст.");
        }
        String data = update.getCallbackQuery().getData();
        ActionType type;
        try {
            type = ActionType.valueOf(data);
            return dispatch(type, update, chatId, userId);
        } catch (IllegalArgumentException e) {
            if (isUUID(data)) {
                historyService.setState(chatId, ActionType.GET_CHILD);
                Event event = eventStorage.getEventById(data);
                historyService.setCurrentEvent(userId, event);
                return dispatch(ActionType.GET_CHILD, update, chatId, userId);
            } else {
                return new SendMessage(chatId.toString(), "❌ Неизвестный callback: " + data);
            }
        }
    }

    private boolean isTextState(ActionType state) {
        return switch (state) {
            case CHILD_DESCRIPTION_CREATION,
                 CHILD_BUTTON_CREATION,
                 ROOT_DESCRIPTION_CREATION,
                 ROOT_BUTTON_CREATION,
                 ENDING_DESCRIPTION_CREATION,
                 ENDING_BUTTON_CREATION -> true;
            default -> false;
        };
    }

    private SendMessage dispatch(ActionType actionType,
                                 Update update, Long chatId, Long userId) {
        ActionType takeAgainActionType;
        Object takeAgainResult;

        Object result = fsmDispatcher.dispatch(actionType, update, chatId, userId);
        log.info("ActionType buttonservice {}", actionType);
        if (result instanceof SendMessage sendMessage) {
            log.info("Result {}", result);
            return sendMessage;
        }

        takeAgainActionType = historyService.getActualState(chatId);
        takeAgainResult = fsmDispatcher.dispatch(takeAgainActionType, update, chatId, userId);
        if (takeAgainResult instanceof SendMessage sendMessage) {
            log.info("TakeAgainResult {}", takeAgainResult);
            return sendMessage;
        }
        return new SendMessage(chatId.toString(), "Ошибка");
    }

    //Проверка, String == UUID?
    private boolean isUUID(String string) {
        if (string == null) return false;
        try {
            UUID.fromString(string);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private ActionType isErrorHandler(Update update, Long chatId, Long userId) {
        ActionType actionType;
        if (update.getMessage().getText().equals(adminCleanDb)) {
            historyService.setState(chatId, ActionType.ADMIN_MODE);
            actionType = ActionType.ADMIN_MODE;
        } else {
            historyService.setState(chatId, ActionType.START);
            actionType = ActionType.START;
        }
        historyService.reset(chatId, userId);
        return actionType;

    }

}