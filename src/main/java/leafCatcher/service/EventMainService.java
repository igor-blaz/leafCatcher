package leafCatcher.service;

import leafCatcher.handlers.FSMDispatcher;
import leafCatcher.history.ActionType;
import leafCatcher.history.HistoryService;
import leafCatcher.model.Event;
import leafCatcher.storage.EventStorage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    public SendMessage makeMessageByText(Update update, Long chatId, Long userId) {
        log.info("By text");
        ActionType actionType = historyService.getActualState(chatId);
        log.info("actualState {}", actionType);
        if(actionType==null){
            historyService.setState(chatId, ActionType.ROOT_IS_ABSENCE_INFO);
        }
        if (actionType == ActionType.ERROR) {
            if (update.getMessage().getText().equals("ff")) {
                log.info("Admin mode");
                historyService.setState(chatId, ActionType.ADMIN_MODE);
                actionType = ActionType.ADMIN_MODE;
            } else {
                log.info("Start");
                historyService.setState(chatId, ActionType.START);
                actionType = ActionType.START;
            }
            historyService.reset(chatId, userId);
        }
        return dispatch(actionType, update, chatId, userId);

    }

    public SendMessage makeMessageByCallback(Update update, Long chatId, Long userId) {
        log.info("CALLBACK🔥");
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

    private boolean isUUID(String string) {
        if (string == null) return false;
        try {
            UUID.fromString(string);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

}