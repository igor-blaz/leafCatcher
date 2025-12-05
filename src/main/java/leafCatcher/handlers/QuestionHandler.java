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
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
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

    @FSMRoute(ActionType.BACK_OR_FORWARD_QUESTION)
    public BotMessage handleRootButton(Update update, Long chatId, Long userId) {
        return messageFactory.makeQuestionMessage(update, chatId, userId, DeleteStrategy.NONE);
    }

    @FSMRoute(ActionType.WRITE_NEXT_QUESTION)
    public BotMessage handleEventNotification(Update update, Long chatId, Long userId) {
        Event current = historyService.getCurrentEvent(userId);
        return messageFactory.makeWriteOrNotMessage(chatId, current, DeleteStrategy.NONE);
    }

    @FSMRoute(ActionType.DO_ACTION)
    public BotMessage handleDoAction(Update update, Long chatId, Long userId) {
        Event current = historyService.getCurrentEvent(userId);
        int size = eventStorage.getChildren(current.getElementId()).size();
        InlineKeyboardMarkup markup = markupFactory.makeActionMarkup(size, userId, current);
        return messageFactory.makeMessage(chatId, markup, "Вот действия", DeleteStrategy.NONE);
    }

    @FSMRoute(ActionType.DELETE)
    public BotMessage handleDeleteEvent(Update update, Long chatId, Long userId) {
        log.info("Dleete handler");
        Event currentEventForDelete = historyService.getCurrentEvent(userId);
        List<Event> childList = eventStorage.getChildren(currentEventForDelete.getElementId());
        String name = GetTelegramUserName.getName(update);
        historyService.setAttemptsToExecute(userId, 2);
        if (!childList.isEmpty()) {
            log.info("Current {}", historyService.getCurrentEvent(userId));
            return messageFactory.makeTextMessage(chatId,
                    name + " вы можете удалять только те события, у которых нет дочерних событий☹️",
                    DeleteStrategy.NONE);
        } else if (!name.equals(currentEventForDelete.getAuthor())) {
            return messageFactory.makeTextMessage(chatId, name + " можно удалять только те события, которые создали вы." +
                    " У этого события другой автор", DeleteStrategy.NONE);
        }
        Event parent = eventStorage.getParent(currentEventForDelete.getElementId());
        if (parent == null) {

            return messageFactory.makeTextMessage(chatId, name + " у этого события нет родительского." +
                    " Ошибка. Нажмите /start", DeleteStrategy.NONE);
        }
        historyService.setCurrentEvent(userId, parent);
        historyService.setState(chatId, ActionType.REPEAT_CURRENT);

        eventStorage.deleteById(currentEventForDelete.getElementId());
        return messageFactory.makeTextMessage(chatId, "Отлично, событие удалено🔥", DeleteStrategy.NONE);
    }


}
