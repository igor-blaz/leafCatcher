package leafCatcher.handlers;

import leafCatcher.history.*;
import leafCatcher.model.Event;
import leafCatcher.service.TextService;
import leafCatcher.service.messageFactory.MarkupFactory;
import leafCatcher.service.messageFactory.MessageFactory;
import leafCatcher.storage.EventStorage;
import leafCatcher.utilityClasses.mapper.EventMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

import java.util.List;

@Slf4j
@Component
public class RootCreateHandler extends AbstractFsmHandler {
    FSMDispatcher fsmDispatcher;

    public RootCreateHandler(HistoryService historyService,
                             MessageFactory messageFactory,
                             MarkupFactory markupFactory,
                             EventStorage eventStorage,
                             TextService textService,
                             DraftService draftService) {
        super(historyService, messageFactory, markupFactory, eventStorage, textService, draftService);
    }

    @FSMRoute(ActionType.ROOT_IS_ABSENCE_INFO)
    public SendMessage handleRootNotification(Update update, Long chatId, Long userId) {
        log.info("Root is absence");
        //1. Проинформировали, что нет корневого события. Переводим в состояние создания кнопки
        historyService.setState(chatId, ActionType.ROOT_BUTTON_CREATION);
        return new SendMessage(chatId.toString(), textService.get("bot.info.thereIsNoRoot"));
    }

    @FSMRoute(ActionType.ROOT_BUTTON_CREATION)
    public SendMessage handleRootButton(Update update, Long chatId, Long userId) {
        //2. Создаем кнопку. Переводим в состояние создания описания
        String buttonName = update.getMessage().getText();
        draftService.setRootButtonName(userId, buttonName);
        historyService.setState(chatId, ActionType.ROOT_DESCRIPTION_CREATION);
        return new SendMessage(chatId.toString(), "Отлично! Кнопка будет называться " + buttonName +
                " теперь ты можешь написать событие");
    }

    @FSMRoute(ActionType.ROOT_DESCRIPTION_CREATION)
    public SendMessage handleRootDescription(Update update, Long chatId, Long userId) {
        //3. Создаем описание. Переводим в состояние Вперед или назад.
        String description = update.getMessage().getText();
        draftService.setRootDescription(userId, description);

        Draft draft = draftService.get(userId);
        Event event = EventMapper.makeRoot(update, description, draft.getRootButtonName());
        eventStorage.saveEvent(event);
        historyService.setCurrentEvent(userId, event);
        historyService.setState(chatId, ActionType.BACK_OR_FORWARD_QUESTION);
        historyService.setAttemptsToExecute(userId, 2);


        return new SendMessage(chatId.toString(), textService.get("bot.info.userCreatedRootDescription"));
    }


    @FSMRoute(ActionType.GET_ROOT)
    public SendMessage handleGetRoot(Update update, Long chatId, Long userId) {
        Event root = eventStorage.getRootEvent();
        if (root == null) {
            log.info("root is null again");
            historyService.setState(chatId, ActionType.ERROR);
            return null;
        }
        log.info("❤️getDescription  {}", root.getDescription());
        historyService.setState(chatId, ActionType.CHILD_DESCRIPTION_AWAIT);
        historyService.setAttemptsToExecute(userId, 2);
        InlineKeyboardMarkup markup = markupFactory.makeMarkup(List.of(root), userId);
        return messageFactory.makeMessage(chatId, markup, root.getDescription());
    }
}
