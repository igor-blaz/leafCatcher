package leafCatcher.handlers;

import leafCatcher.history.ActionType;
import leafCatcher.history.DraftService;
import leafCatcher.history.FSMRoute;
import leafCatcher.history.HistoryService;
import leafCatcher.model.Event;
import leafCatcher.service.MessageService;
import leafCatcher.service.messageFactory.MarkupFactory;
import leafCatcher.service.messageFactory.MessageFactory;
import leafCatcher.storage.EventStorage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

import java.util.List;

@Component
@Slf4j
public class GoBackHandler extends AbstractFsmHandler {
    public GoBackHandler(HistoryService historyService,
                         MessageFactory messageFactory,
                         MarkupFactory markupFactory,
                         EventStorage eventStorage,
                         MessageService messageService,
                         DraftService draftService) {
        super(historyService, messageFactory, markupFactory, eventStorage, messageService, draftService);
    }

    @FSMRoute(ActionType.GO_BACK)
    public SendMessage goBack(Update update, Long chatId, Long userId) {
        historyService.setState(chatId, ActionType.GET_CHILD);
        Event child = historyService.getCurrentEvent(userId);
        Event parent = eventStorage.getParent(child.getElementId());
        historyService.setCurrentEvent(userId, parent);

        List<Event> patentsChildren = eventStorage.getChildren(parent.getElementId());
        InlineKeyboardMarkup markup = markupFactory.makeMarkup(patentsChildren, userId);
        return messageFactory.makeMessage(chatId, markup, parent.getDescription());
    }
}
