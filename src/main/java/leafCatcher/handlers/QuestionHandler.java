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
import leafCatcher.utilityClasses.GetTelegramUserName;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

import java.util.List;

@Component
@Slf4j
public class QuestionHandler extends AbstractFsmHandler {
    public QuestionHandler(HistoryService historyService,
                           MessageFactory messageFactory,
                           MarkupFactory markupFactory,
                           EventStorage eventStorage,
                           TextService textService,
                           DraftService draftService) {
        super(historyService, messageFactory, markupFactory,
                eventStorage, textService, draftService);

    }

    private static final String ADMIN_NAME = "Blaz201";

    @FSMRoute(ActionType.BACK_OR_FORWARD_QUESTION)
    public BotMessage handleRootButton(Update update, Long chatId, Long userId) {
        int hp = ActionType.BACK_OR_FORWARD_QUESTION.getLifeTime();
        DeleteStrategy deleteStrategy = ActionType.BACK_OR_FORWARD_QUESTION.getDeleteStrategy();
        return messageFactory.makeQuestionMessage(update, chatId, userId, deleteStrategy, hp);
    }

    @FSMRoute(ActionType.WRITE_NEXT_QUESTION)
    public BotMessage handleEventNotification(Update update, Long chatId, Long userId) {
        int hp = ActionType.WRITE_NEXT_QUESTION.getLifeTime();
        DeleteStrategy deleteStrategy = ActionType.WRITE_NEXT_QUESTION.getDeleteStrategy();
        Event current = historyService.getCurrentEvent(userId);
        return messageFactory.makeWriteOrNotMessage(chatId, userId, current, deleteStrategy, hp);
    }

    @FSMRoute(ActionType.DO_ACTION)
    public BotMessage handleDoAction(Update update, Long chatId, Long userId) {
        int hp = ActionType.DO_ACTION.getLifeTime();
        DeleteStrategy deleteStrategy = ActionType.DO_ACTION.getDeleteStrategy();
        Event current = historyService.getCurrentEvent(userId);
        int size = eventStorage.getChildren(current.getElementId()).size();
        InlineKeyboardMarkup markup = markupFactory.makeActionMarkup(size, userId, current);
        return messageFactory.makeMessage(chatId, markup, "Вот действия", deleteStrategy, hp);
    }

    @FSMRoute(ActionType.DELETE)
    public BotMessage handleDeleteEvent(Update update, Long chatId, Long userId) {
        log.info("Dleete handler");
        int hp = ActionType.DELETE.getLifeTime();
        DeleteStrategy deleteStrategy = ActionType.DELETE.getDeleteStrategy();
        Event currentEventForDelete = historyService.getCurrentEvent(userId);

        List<Event> childList = eventStorage.getChildren(currentEventForDelete.getElementId());
        String name = GetTelegramUserName.getName(update);
        historyService.setAttemptsToExecute(userId, 2);

        if (hasNonDummyEvent(childList)) {
            log.info("Current {}", historyService.getCurrentEvent(userId));
            return messageFactory.makeTextMessage(chatId,
                    name + " вы можете удалять только те события, у которых нет дочерних событий☹️",
                    DeleteStrategy.DELETE_BY_HP, hp);
        } else if (!name.equals(currentEventForDelete.getAuthor()) &&
                !name.equals(ADMIN_NAME)) {
            return messageFactory.makeTextMessage(chatId, name + " можно удалять только те события, которые создали вы." +
                    " У этого события другой автор", DeleteStrategy.DELETE_BY_HP, hp);
        }
        Event parent = eventStorage.getParent(currentEventForDelete.getElementId());
        if (parent == null) {

            return messageFactory.makeTextMessage(chatId, name + " у этого события нет родительского." +
                    " Ошибка. Нажмите /start", DeleteStrategy.DELETE_BY_HP, hp);
        }
        historyService.setCurrentEvent(userId, parent);
        historyService.setState(chatId, ActionType.REPEAT_CURRENT);
        if (!childList.isEmpty()) {
            for (Event event : childList) {
                eventStorage.deleteById(event.getElementId());
            }
        }
        eventStorage.deleteById(currentEventForDelete.getElementId());
        return messageFactory.makeTextMessage(chatId, "Отлично, событие удалено🔥", deleteStrategy, hp);
    }

    public boolean hasNonDummyEvent(List<Event> events) {
        return events.stream().anyMatch(event -> !event.getIsDummy());
    }

}
