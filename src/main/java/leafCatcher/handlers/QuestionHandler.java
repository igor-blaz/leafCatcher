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

@Component
@Slf4j
public class QuestionHandler extends AbstractFsmHandler {
    public QuestionHandler(HistoryService historyService,
                           MessageFactory messageFactory,
                           MarkupFactory markupFactory,
                           EventStorage eventStorage,
                           MessageService messageService,
                           DraftService draftService) {
        super(historyService, messageFactory, markupFactory,
                eventStorage, messageService, draftService);
    }

    @FSMRoute(ActionType.BACK_OR_FORWARD_QUESTION)
    public SendMessage handleRootButton(Update update, Long chatId, Long userId) {
        return messageFactory.makeQuestionMessage(chatId, userId);
    }

    @FSMRoute(ActionType.WRITE_NEXT_QUESTION)
    public SendMessage handleEventNotification(Update update, Long chatId, Long userId) {
        Event current = historyService.getCurrentEvent(userId);
        return messageFactory.makeWriteOrNotMessage(chatId, current);
    }

    @FSMRoute(ActionType.DO_ACTION)
    public SendMessage handleDoAction(Update update, Long chatId, Long userId) {
        Event current = historyService.getCurrentEvent(userId);
        int size = eventStorage.getChildren(current.getElementId()).size();
        InlineKeyboardMarkup markup = markupFactory.makeActionMarkup(size, userId, current);
        return messageFactory.makeMessage(chatId, markup, "Вот действия");
    }


}
