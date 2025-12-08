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
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
@Slf4j
public class HelpHandler extends AbstractFsmHandler {
    public HelpHandler(HistoryService historyService,
                       MessageFactory messageFactory,
                       MarkupFactory markupFactory,
                       EventStorage eventStorage,
                       TextService textService,
                       DraftService draftService) {
        super(historyService, messageFactory, markupFactory, eventStorage, textService, draftService);
    }

    @FSMRoute(ActionType.I_DONT_KNOW)
    public BotMessage handleIDontKnow(Update update, Long chatId, Long userId) {
        int hp = ActionType.I_DONT_KNOW.getLifeTime();
        historyService.setAttemptsToExecute(userId, 2);
        return messageFactory.makeIDontKnowMessage(chatId, userId, DeleteStrategy.DELETE_AFTER_N_MESSAGES, hp);
    }
}
