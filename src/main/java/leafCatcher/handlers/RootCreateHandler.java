package leafCatcher.handlers;

import leafCatcher.history.*;
import leafCatcher.model.Event;
import leafCatcher.service.TextService;
import leafCatcher.service.deleteStrategy.BotMessage;
import leafCatcher.service.deleteStrategy.DeleteStrategy;
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
    public BotMessage handleRootNotification(Update update, Long chatId, Long userId) {
        log.info("Root is absence");
        int hp = ActionType.ROOT_IS_ABSENCE_INFO.getLifeTime();
        DeleteStrategy deleteStrategy = ActionType.ROOT_IS_ABSENCE_INFO.getDeleteStrategy();
        //1. Проинформировали, что нет корневого события. Переводим в состояние создания кнопки
        historyService.setState(chatId, ActionType.ROOT_BUTTON_CREATION);
        SendMessage sendMessage = new SendMessage(chatId.toString(), textService.get("bot.info.thereIsNoRoot"));
        return new BotMessage(sendMessage, deleteStrategy, hp);
    }

    @FSMRoute(ActionType.ROOT_BUTTON_CREATION)
    public BotMessage handleRootButton(Update update, Long chatId, Long userId) {
        //2. Создаем кнопку. Переводим в состояние создания описания
        int hp = ActionType.ROOT_BUTTON_CREATION.getLifeTime();
        DeleteStrategy deleteStrategy = ActionType.ROOT_BUTTON_CREATION.getDeleteStrategy();
        String buttonName = update.getMessage().getText();
        draftService.setRootButtonName(userId, buttonName);
        historyService.setState(chatId, ActionType.ROOT_DESCRIPTION_CREATION);
        SendMessage sendMessage = new SendMessage(chatId.toString(), "Отлично! Кнопка будет называться " + buttonName +
                " теперь ты можешь написать событие");
        return new BotMessage(sendMessage, deleteStrategy, hp);
    }

    @FSMRoute(ActionType.ROOT_DESCRIPTION_CREATION)
    public BotMessage handleRootDescription(Update update, Long chatId, Long userId) {
        //3. Создаем описание. Переводим в состояние Вперед или назад.
        int hp = ActionType.ROOT_DESCRIPTION_CREATION.getLifeTime();
        DeleteStrategy deleteStrategy = ActionType.ROOT_DESCRIPTION_CREATION.getDeleteStrategy();
        String description = update.getMessage().getText();
        draftService.setRootDescription(userId, description);

        Draft draft = draftService.get(userId);
        Event event = EventMapper.makeRoot(update, description, draft.getRootButtonName());
        eventStorage.saveEvent(event);
        historyService.setCurrentEvent(userId, event);
        historyService.setState(chatId, ActionType.BACK_OR_FORWARD_QUESTION);
        historyService.setAttemptsToExecute(userId, 2);


        SendMessage sendMessage = new SendMessage(chatId.toString(), textService.get("bot.info.userCreatedRootDescription"));
        return new BotMessage(sendMessage, deleteStrategy, hp);
    }


    @FSMRoute(ActionType.GET_ROOT)
    public BotMessage handleGetRoot(Update update, Long chatId, Long userId) {
        int hp = ActionType.GET_ROOT.getLifeTime();
        DeleteStrategy deleteStrategy = ActionType.GET_ROOT.getDeleteStrategy();
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
        return messageFactory.makeEventMessage(chatId, markup, root,
                deleteStrategy, hp);
    }
}
