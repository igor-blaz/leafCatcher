package leafCatcher.handlers;

import leafCatcher.history.ActionType;
import leafCatcher.history.DraftService;
import leafCatcher.history.HistoryService;
import leafCatcher.service.TextService;
import leafCatcher.service.deleteStrategy.BotMessage;
import leafCatcher.service.deleteStrategy.DeleteStrategy;
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

    protected SendMessage rejectCallbackWhenExpectingText(Update update, Long chatId, String expectedHint) {
        if (hasCallback(update)) {
            //log.warn("AntiClick: stale callback received when expecting text. data={}", getCallbackData(update));
            return new SendMessage(
                    chatId.toString(),
                    "Эта кнопка уже неактуальна 🙂\nСейчас я жду от тебя " + expectedHint
            );
        }
        return null;
    }

    protected String getCallbackData(Update update) {
        return hasCallback(update) ? update.getCallbackQuery().getData() : null;
    }

    protected BotMessage wrongInput(Long chatId, String expected, DeleteStrategy deleteStrategy, int hp) {
        SendMessage sendMessage = new SendMessage(
                chatId.toString(),
                "Сейчас я жду от тебя " + expected + " 🙂"
        );
        return new BotMessage(sendMessage, deleteStrategy, hp);
    }

    protected void goToEnding(Update update, Long chatId, Long userId) {
        log.warn("IS END");
        historyService.setAttemptsToExecute(userId, 2);
        historyService.setState(chatId, ActionType.GET_ENDING);
    }

    protected BotMessage handleNoChildren(Update update,
                                          Long chatId,
                                          Long userId,
                                          DeleteStrategy deleteStrategy,
                                          int hp) {
        log.warn("no children events");
        historyService.setAttemptsToExecute(userId, 2);
        historyService.setState(chatId, ActionType.WRITE_NEXT_QUESTION);
        SendMessage sendMessage = new SendMessage(chatId.toString(), "Продолжение еще не написано");
        return new BotMessage(sendMessage, DeleteStrategy.DELETE_AFTER_ONE, 3);
    }

}
