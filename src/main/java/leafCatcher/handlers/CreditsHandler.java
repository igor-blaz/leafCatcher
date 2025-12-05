package leafCatcher.handlers;

import leafCatcher.history.ActionType;
import leafCatcher.history.DraftService;
import leafCatcher.history.FSMRoute;
import leafCatcher.history.HistoryService;
import leafCatcher.service.TextService;
import leafCatcher.service.deleteStrategy.BotMessage;
import leafCatcher.service.deleteStrategy.DeleteStrategy;
import leafCatcher.service.messageFactory.MarkupFactory;
import leafCatcher.service.messageFactory.MessageFactory;
import leafCatcher.storage.EventStorage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;


@Component
@Slf4j
public class CreditsHandler extends AbstractFsmHandler {


    public CreditsHandler(HistoryService historyService,
                          MessageFactory messageFactory,
                          MarkupFactory markupFactory,
                          EventStorage eventStorage,
                          TextService textService,
                          DraftService draftService) {
        super(historyService, messageFactory, markupFactory, eventStorage, textService, draftService);
    }

    @FSMRoute(ActionType.CREDITS)
    public BotMessage handleAfterParty(Update update, Long chatId, Long userId) {
        historyService.setAttemptsToExecute(userId, 2);
        historyService.setState(chatId, ActionType.AFTER_END_CHOICE);
        String credits = textService.getMarkdown("ru.bot.info.credits");
        return messageFactory.makeTextMessage(chatId, credits, DeleteStrategy.NONE);
    }
}
