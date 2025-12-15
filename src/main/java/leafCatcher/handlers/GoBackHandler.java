package leafCatcher.handlers;

import leafCatcher.history.ActionType;
import leafCatcher.history.DraftService;
import leafCatcher.history.FSMRoute;
import leafCatcher.history.HistoryService;
import leafCatcher.model.Event;
import leafCatcher.service.TextService;
import leafCatcher.service.deleteStrategy.BotMessage;
import leafCatcher.service.deleteStrategy.DeleteMessageService;
import leafCatcher.service.deleteStrategy.DeleteStrategy;
import leafCatcher.service.messageFactory.MarkupFactory;
import leafCatcher.service.messageFactory.MessageFactory;
import leafCatcher.storage.EventStorage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

import java.util.List;

@Component
@Slf4j
public class GoBackHandler extends AbstractFsmHandler {
    private final DeleteMessageService deleteMessageService;

    public GoBackHandler(HistoryService historyService,
                         MessageFactory messageFactory,
                         MarkupFactory markupFactory,
                         EventStorage eventStorage,
                         TextService textService,
                         DraftService draftService,
                         DeleteMessageService deleteMessageService) {

        super(historyService,
                messageFactory,
                markupFactory,
                eventStorage,
                textService,
                draftService);
        this.deleteMessageService = deleteMessageService;
    }

    @FSMRoute(ActionType.GO_BACK)
    public BotMessage goBack(Update update, Long chatId, Long userId) {
        int hp = ActionType.GO_BACK.getLifeTime();
        DeleteStrategy deleteStrategy = ActionType.GO_BACK.getDeleteStrategy();
        historyService.setState(chatId, ActionType.GET_CHILD);
        Event child = historyService.getCurrentEvent(userId);
        Event parent = eventStorage.getParent(child.getElementId());
        historyService.setCurrentEvent(userId, parent);

      //  deleteMessageService.deleteEventMessageFromChat(chatId, child);

        List<Event> patentsChildren = eventStorage.getChildren(parent.getElementId());
        InlineKeyboardMarkup markup = markupFactory.makeMarkup(patentsChildren, userId);
        return messageFactory.makeEventMessage(chatId,
                markup,
                parent,
                deleteStrategy,
                hp);
    }
}
