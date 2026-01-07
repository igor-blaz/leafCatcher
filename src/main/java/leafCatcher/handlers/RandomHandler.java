package leafCatcher.handlers;

import leafCatcher.history.ActionType;
import leafCatcher.history.DraftService;
import leafCatcher.history.FSMRoute;
import leafCatcher.history.HistoryService;
import leafCatcher.model.Event;
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
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

import java.util.List;

@Slf4j
@Component
public class RandomHandler extends AbstractFsmHandler {
    public RandomHandler(HistoryService historyService, MessageFactory messageFactory, MarkupFactory markupFactory, EventStorage eventStorage, TextService textService, DraftService draftService) {
        super(historyService, messageFactory, markupFactory, eventStorage, textService, draftService);
    }

    @FSMRoute(ActionType.RANDOM)
    public BotMessage handleEventNotification(Update update, Long chatId, Long userId) {
        log.warn("RANDOM");
        int hp = ActionType.RANDOM.getLifeTime();
        DeleteStrategy deleteStrategy = ActionType.RANDOM.getDeleteStrategy();
        Event parent = eventStorage.getRandom();
        if (parent == null || parent.getIsDummy()) {
            parent = eventStorage.getRootEvent();
        }
        List<Event> children = eventStorage.getChildren(parent.getElementId());
        if (children == null || children.isEmpty()) {
            return handleNoChildren(update, chatId, userId, DeleteStrategy.DELETE_ON_NEXT, hp);
        }
        historyService.setState(chatId, ActionType.RANDOM);
        InlineKeyboardMarkup markup = markupFactory.makeMarkup(children, userId);
        return messageFactory.makeEventMessage(chatId, markup, parent,
                deleteStrategy, hp);
    }
}
