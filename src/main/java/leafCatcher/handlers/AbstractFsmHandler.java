package leafCatcher.handlers;

import leafCatcher.history.ActionType;
import leafCatcher.history.DraftService;
import leafCatcher.history.HistoryService;
import leafCatcher.service.TextService;
import leafCatcher.service.messageFactory.MarkupFactory;
import leafCatcher.service.messageFactory.MessageFactory;
import leafCatcher.storage.EventStorage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

@Slf4j
@RequiredArgsConstructor
@Component
public abstract class AbstractFsmHandler {

    @Value("${admin.secret.command.cleanNeo4j}")
    public String adminCleanDb;

    protected final HistoryService historyService;
    protected final MessageFactory messageFactory;
    protected final MarkupFactory markupFactory;
    protected final EventStorage eventStorage;
    protected final TextService textService;
    protected final DraftService draftService;

    protected boolean hasText(Update update) {
        return update != null &&
                update.hasMessage() &&
                update.getMessage().hasText();
    }

    protected boolean hasCallback(Update update) {
        return update != null &&
                update.hasCallbackQuery() &&
                update.getCallbackQuery().getData() != null;
    }
    protected SendMessage expectTextOrReject(Update update, Long chatId, String expectedHint) {
        if (hasCallback(update)) {
            log.warn("AntiClick: callback received when text expected. data={}", getCallbackData(update));
            return wrongInput(chatId, expectedHint);
        }
        if (!hasText(update)) {
            return wrongInput(chatId, expectedHint);
        }
        return null; // значит всё ок, хендлер продолжает
    }
    protected SendMessage rejectStaleCallbackWhenTextExpected(Update update, Long chatId, String expectedHint) {
        if (hasCallback(update)) {
            log.warn("AntiClick: stale callback when text expected. data={}", getCallbackData(update));
            return wrongInput(chatId, expectedHint);
        }
        return null;
    }


    protected SendMessage expectCallbackOrReject(Update update, Long chatId, String expectedHint) {
        if (hasText(update)) {
            log.warn("AntiClick: text received when callback expected. text={}", getText(update));
            return wrongInput(chatId, expectedHint);
        }
        if (!hasCallback(update)) {
            return wrongInput(chatId, expectedHint);
        }
        return null;
    }

    protected SendMessage rejectCallbackWhenExpectingText(Update update, Long chatId, String expectedHint) {
        if (hasCallback(update)) {
            log.warn("AntiClick: stale callback received when expecting text. data={}", getCallbackData(update));
            return new SendMessage(
                    chatId.toString(),
                    "Эта кнопка уже неактуальна 🙂\nСейчас я жду от тебя " + expectedHint
            );
        }
        return null;
    }

    protected String getText(Update update) {
        return hasText(update) ? update.getMessage().getText() : null;
    }

    protected String getCallbackData(Update update) {
        return hasCallback(update) ? update.getCallbackQuery().getData() : null;
    }

    protected SendMessage wrongInput(Long chatId, String expected) {
        return new SendMessage(
                chatId.toString(),
                "Сейчас я жду от тебя " + expected + " 🙂"
        );
    }

    protected void goToEnding(Update update, Long chatId, Long userId) {
        log.warn("IS END");
        historyService.setAttemptsToExecute(userId, 2);
        historyService.setState(chatId, ActionType.GET_ENDING);
    }

    protected SendMessage handleNoChildren(Update update, Long chatId, Long userId) {
        log.warn("no children events");
        historyService.setAttemptsToExecute(userId, 2);
        historyService.setState(chatId, ActionType.WRITE_NEXT_QUESTION);
        return new SendMessage(chatId.toString(), "Продолжение еще не написано");
    }

    protected void handleNoRoot(Update update, Long chatId, Long userId) {
        historyService.setState(chatId, ActionType.ROOT_IS_ABSENCE_INFO);
        historyService.setAttemptsToExecute(userId, 2);
    }
}
