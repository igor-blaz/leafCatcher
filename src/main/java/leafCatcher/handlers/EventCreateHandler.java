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
import leafCatcher.utilityClasses.mapper.EventMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

import java.util.List;

@Slf4j
@Component
public class EventCreateHandler extends AbstractFsmHandler {

    public EventCreateHandler(HistoryService historyService,
                              MessageFactory messageFactory,
                              MarkupFactory markupFactory,
                              EventStorage eventStorage,
                              TextService textService,
                              DraftService draftService) {
        super(historyService, messageFactory, markupFactory, eventStorage, textService, draftService);
    }

    //Этот метод вызывается кнопкой написать продолжение
    @FSMRoute(ActionType.CHILD_DESCRIPTION_AWAIT)
    public BotMessage handleAwaitDescription(Update update, Long chatId, Long userId) {
        //1. Информируем, что нет продолжения. Переводим в состояние создания кнопки
        int hp = ActionType.CHILD_DESCRIPTION_AWAIT.getLifeTime();
        DeleteStrategy deleteStrategy = ActionType.CHILD_DESCRIPTION_AWAIT.getDeleteStrategy();
        historyService.setState(chatId, ActionType.CHILD_BUTTON_CREATION);
        return messageFactory.makeTextMessage(chatId,
                "Хорошо 🌿 Озаглавь следующее событие ✨",
                deleteStrategy, hp);
    }


    @FSMRoute(ActionType.CHILD_DESCRIPTION_CREATION)
    public BotMessage handleEventDescription(Update update, Long chatId, Long userId) {
        SendMessage reject = rejectCallbackWhenExpectingText(update, chatId, "текст описания события");
        int hp = ActionType.CHILD_DESCRIPTION_CREATION.getLifeTime();
        DeleteStrategy deleteStrategy = ActionType.CHILD_DESCRIPTION_CREATION.getDeleteStrategy();
        if (reject != null) {
            return new BotMessage(reject, DeleteStrategy.DELETE_ON_NEXT, hp);
        }
        if (!hasText(update)) {
            return wrongInput(chatId, "текст описания события", DeleteStrategy.DELETE_ON_NEXT, hp);
        }
        String description = update.getMessage().getText();
        draftService.setChildDescription(userId, description);
        String buttonName = draftService.getChildButtonName(userId);
        Event parent = historyService.getCurrentEvent(userId);
        if (parent == null) {
            return messageFactory.makeTextMessage(chatId,
                    "Не могу создать кнопку: не найден родительский лист 🥲",
                    DeleteStrategy.DELETE_ON_NEXT, hp);
        }

        Event child = EventMapper.makeEvent(update, description, buttonName, false);
        child = eventStorage.saveChild(parent.getElementId(), child);

        historyService.setCurrentEvent(userId, child);
        historyService.setAttemptsToExecute(chatId, 2);
        historyService.setState(chatId, ActionType.BACK_OR_FORWARD_QUESTION);
        SendMessage sendMessage = new SendMessage(
                chatId.toString(),
                textService.get("bot.info.userCreatedChildDescription")
        );
        return new BotMessage(sendMessage, deleteStrategy, hp);
    }

    @FSMRoute(ActionType.CHILD_BUTTON_CREATION)
    public BotMessage handleRootButton(Update update, Long chatId, Long userId) {
        int hp = ActionType.CHILD_BUTTON_CREATION.getLifeTime();
        DeleteStrategy deleteStrategy = ActionType.CHILD_BUTTON_CREATION.getDeleteStrategy();
        SendMessage reject = rejectCallbackWhenExpectingText(update, chatId, "текст описания события");
        if (reject != null) {
            return new BotMessage(reject, DeleteStrategy.DELETE_ON_NEXT, hp);
        }
        if (!hasText(update)) {
            return wrongInput(chatId, "текст описания события", DeleteStrategy.DELETE_ON_NEXT, hp);
        }
        String buttonName = update.getMessage().getText();
        draftService.setChildButtonName(userId, buttonName);
        historyService.setState(chatId, ActionType.CHILD_DESCRIPTION_CREATION);
        SendMessage sendMessage = new SendMessage(chatId.toString(),
                "Отлично! Кнопка будет называться " + buttonName + " теперь напиши событие 🪶");
        return new BotMessage(sendMessage, deleteStrategy, hp);
    }


    @FSMRoute(ActionType.GET_CHILD)
    public BotMessage handleGetRoot(Update update, Long chatId, Long userId) {
        log.info("гет child метод");
        int hp = ActionType.GET_CHILD.getLifeTime();
        DeleteStrategy deleteStrategy = ActionType.GET_CHILD.getDeleteStrategy();
        Event parent = historyService.getCurrentEvent(userId);
        if (parent.getIsDummy()) {
            parent = eventStorage.getEventById(parent.getOriginalId());
        }
        if (parent.getIsEnd()) {
            goToEnding(update, chatId, userId);
            log.error("!!!!!!!!!!!!!!!!!!!!!!");
            return null;
        }
        List<Event> children = eventStorage.getChildren(parent.getElementId());
        if (children.isEmpty() && !parent.getIsEnd()) {
            return handleNoChildren(update, chatId, userId, DeleteStrategy.DELETE_BY_HP, hp);
        }
        InlineKeyboardMarkup markup = markupFactory.makeMarkup(children, userId);
        return messageFactory.makeEventMessage(chatId, markup, parent,
                deleteStrategy, hp);
    }

    @FSMRoute(ActionType.REPEAT_CURRENT)
    public BotMessage handleGetCurrent(Update update, Long chatId, Long userId) {
        int hp = ActionType.REPEAT_CURRENT.getLifeTime();
        DeleteStrategy deleteStrategy = ActionType.REPEAT_CURRENT.getDeleteStrategy();
        Event current = historyService.getCurrentEvent(userId);
        if (current == null) {
            log.warn("Текущее событие не найдено для пользователя {}", userId);
            SendMessage sendMessage = new SendMessage(chatId.toString(), "Не найдено текущее событие 🥲");
            return new BotMessage(sendMessage, DeleteStrategy.DELETE_ON_NEXT, hp);
        }
        List<Event> children = eventStorage.getChildren(current.getElementId());
        InlineKeyboardMarkup markup = markupFactory.makeMarkup(children, userId);
        return messageFactory.makeEventMessage(chatId, markup, current, deleteStrategy, hp);
    }
}


