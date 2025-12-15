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

@Slf4j
@Component
public class StartHandler extends AbstractFsmHandler {

    public StartHandler(HistoryService historyService,
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
    }

    @FSMRoute(ActionType.START)
    public BotMessage handleStart(Update update, Long chatId, Long userId) {
        log.info("Start Handler");

        int hp = ActionType.START.getLifeTime();
        DeleteStrategy deleteStrategy = ActionType.START.getDeleteStrategy();
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
            historyService.setState(chatId, ActionType.CHILD_DESCRIPTION_AWAIT);
            return null;
        }
        log.info("children {}", children);
        InlineKeyboardMarkup markup = markupFactory.makeMarkup(children, userId);
        //deleteMessageService.deleteAllChat(chatId);
        return messageFactory.makeMessage(chatId, markup, root.getDescription(),
                deleteStrategy, hp);
    }

    @FSMRoute(ActionType.INTRO)
    public BotMessage handleIntro(Update update, Long chatId, Long userId) {
        log.info("Info Handler");
        int hp = ActionType.INTRO.getLifeTime();
        DeleteStrategy deleteStrategy = ActionType.INTRO.getDeleteStrategy();
        historyService.setSkipStart(userId);
        historyService.setState(chatId, ActionType.START);
        historyService.setAttemptsToExecute(userId, 2);
        String text = textService.getMarkdown("ru.bot.info.intro");
        return messageFactory.makeTextMessage(chatId, text, deleteStrategy, hp);
    }
}
