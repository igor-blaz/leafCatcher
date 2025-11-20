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

@Slf4j
@Component
public class StartHandler extends AbstractFsmHandler {


    public StartHandler(HistoryService historyService
            , MessageFactory messageFactory,
                        MarkupFactory markupFactory,
                        EventStorage eventStorage,
                        MessageService messageService,
                        DraftService draftService) {
        super(historyService, messageFactory, markupFactory, eventStorage, messageService, draftService);
    }

    @FSMRoute(ActionType.START)
    public SendMessage handleStart(Update update, Long chatId, Long userId) {

        historyService.setZeroAttempts(userId);
        Event root = eventStorage.getRootEvent();

        if (root == null) {
            log.info("⚠️ root is null");
            historyService.setState(chatId, ActionType.ROOT_IS_ABSENCE_INFO);
            return null;
        }
        historyService.setCurrentEvent(userId, root);
        List<Event> children = eventStorage.getChildren(root.getElementId());
        if (children.isEmpty()) {
            log.info("⚠️roots children is empty ");
            historyService.setState(chatId, ActionType.CHILD_IS_ABSENCE_INFO);
            return null;
        }
        log.info("children {}", children);
        InlineKeyboardMarkup markup = markupFactory.makeMarkup(children, userId);
        return messageFactory.makeMessage(chatId, markup, root.getDescription());
    }
}
