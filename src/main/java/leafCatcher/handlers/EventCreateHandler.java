package leafCatcher.handlers;

import leafCatcher.history.ActionType;
import leafCatcher.history.DraftService;
import leafCatcher.history.FSMRoute;
import leafCatcher.history.HistoryService;
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
    public SendMessage handleAwaitDescription(Update update, Long chatId, Long userId) {

        historyService.setState(chatId, ActionType.CHILD_DESCRIPTION_CREATION);
        return new SendMessage(
                chatId.toString(),
                "Хорошо 🌿 Напиши, что произойдёт дальше ✨"
        );
    }


    @FSMRoute(ActionType.CHILD_DESCRIPTION_CREATION)
    public SendMessage handleEventDescription(Update update, Long chatId, Long userId) {
        SendMessage reject = rejectCallbackWhenExpectingText(update, chatId, "текст описания события");
        if (reject != null) {
            return reject;
        }
        if (!hasText(update)) {
            return wrongInput(chatId, "текст описания события");
        }
        String description = update.getMessage().getText();
        draftService.setChildDescription(userId, description);
        historyService.setState(chatId, ActionType.CHILD_BUTTON_CREATION);
        log.info("description {}", description);
        return new SendMessage(
                chatId.toString(),
                textService.get("bot.info.userCreatedChildDescription")
        );
    }

    @FSMRoute(ActionType.CHILD_BUTTON_CREATION)
    public SendMessage handleRootButton(Update update, Long chatId, Long userId) {
        SendMessage reject = rejectCallbackWhenExpectingText(update, chatId, "текст описания события");
        if (reject != null) {
            return reject;
        }
        if (!hasText(update)) {
            return wrongInput(chatId, "текст описания события");
        }
        log.warn("button Creation");
        String buttonName = update.getMessage().getText();
        String description = draftService.getChildDescription(userId);
        Event parent = historyService.getCurrentEvent(userId);
        if (parent == null) {
            return new SendMessage(chatId.toString(),
                    "Не могу создать кнопку: не найден родительский лист 🥲");
        }
        Event child = EventMapper.makeEvent(update, description, buttonName, false);
        child = eventStorage.saveChild(parent.getElementId(), child);
        historyService.setCurrentEvent(chatId, child);
        historyService.setAttemptsToExecute(chatId, 2);
        historyService.setState(chatId, ActionType.BACK_OR_FORWARD_QUESTION);
        return new SendMessage(chatId.toString(),
                "Отлично! Кнопка будет называться " + buttonName);
    }


    @FSMRoute(ActionType.GET_CHILD)
    public SendMessage handleGetRoot(Update update, Long chatId, Long userId) {
        log.info("гет child метод");
        Event parent = historyService.getCurrentEvent(userId);
        if (parent.getIsEnd()) {
            goToEnding(update, chatId, userId);
            return null;
        }
        List<Event> children = eventStorage.getChildren(parent.getElementId());
        if (children.isEmpty() && !parent.getIsEnd()) {
            return handleNoChildren(update, chatId, userId);
        }
        InlineKeyboardMarkup markup = markupFactory.makeMarkup(children, userId);
        return messageFactory.makeMessage(chatId, markup, parent.getDescription());
    }

    @FSMRoute(ActionType.REPEAT_CURRENT)
    public SendMessage handleGetCurrent(Update update, Long chatId, Long userId) {
        Event current = historyService.getCurrentEvent(userId);
        if (current == null) {
            log.warn("Текущее событие не найдено для пользователя {}", userId);
            return new SendMessage(chatId.toString(), "Не найдено текущее событие 🥲");
        }
        List<Event> children = eventStorage.getChildren(current.getElementId());
        InlineKeyboardMarkup markup = markupFactory.makeMarkup(children, userId);
        return messageFactory.makeMessage(chatId, markup, current.getDescription());
    }
}


