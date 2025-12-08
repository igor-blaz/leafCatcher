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

@Slf4j
@Component
public class EndingHandler extends AbstractFsmHandler {
    public EndingHandler(HistoryService historyService,
                         MessageFactory messageFactory,
                         MarkupFactory markupFactory,
                         EventStorage eventStorage,
                         TextService textService,
                         DraftService draftService) {
        super(historyService, messageFactory, markupFactory, eventStorage, textService, draftService);
    }

    @FSMRoute(ActionType.END_IS_ABSENCE_INFO)
    public BotMessage handleNoEndInfo(Update update, Long chatId, Long userId) {
        //1. Уведомляем, что концовки нет. Переходим в состояние создания кнопки
        historyService.setState(chatId, ActionType.ENDING_BUTTON_CREATION);
        SendMessage sendMessage = new SendMessage(
                chatId.toString(),
                textService.get("bot.info.userWantsCreateEnd")
        );
        int hp = ActionType.END_IS_ABSENCE_INFO.getLifeTime();
        return new BotMessage(sendMessage, DeleteStrategy.DELETE_ON_NEXT, hp);
    }

    @FSMRoute(ActionType.ENDING_BUTTON_CREATION)
    public BotMessage handleRootButton(Update update, Long chatId, Long userId) {
        int hp = ActionType.ENDING_BUTTON_CREATION.getLifeTime();
        SendMessage reject = rejectCallbackWhenExpectingText(update, chatId, "текст описания события");
        if (reject != null) {
            return new BotMessage(reject, DeleteStrategy.NONE, hp);
        }
        if (!hasText(update)) {
            return wrongInput(chatId, "текст описания события", DeleteStrategy.DELETE_ON_NEXT, 0);
        }

        String button = update.getMessage().getText();
        draftService.setEndingButtonName(userId, button);
        historyService.setState(chatId, ActionType.ENDING_DESCRIPTION_CREATION);
        return messageFactory.makeTextMessage(chatId,
                textService.get("bot.info.endingButtonCreation"),
                DeleteStrategy.NONE, hp);
    }

    @FSMRoute(ActionType.ENDING_DESCRIPTION_CREATION)
    public BotMessage handleEndDescription(Update update, Long chatId, Long userId) {
        int hp = ActionType.ENDING_DESCRIPTION_CREATION.getLifeTime();
        SendMessage reject = rejectCallbackWhenExpectingText(update, chatId, "текст описания события");
        if (reject != null) {
            return new BotMessage(reject, DeleteStrategy.NONE, hp);
        }
        if (!hasText(update)) {
            return wrongInput(chatId, "текст описания события", DeleteStrategy.NONE, hp);
        }


        String description = update.getMessage().getText();
        String buttonName = draftService.getEndingButtonName(userId);
        Event parent = historyService.getCurrentEvent(userId);
        if (parent == null) {
            return messageFactory.makeTextMessage(chatId,
                    "Не могу создать кнопку: не найден родительский лист 🥲",
                    DeleteStrategy.NONE, hp);
        }
        Event ending = EventMapper.makeEvent(update, description, buttonName, true);
        eventStorage.saveChild(parent.getElementId(), ending);
        historyService.setCurrentEvent(chatId, ending);
        historyService.setAttemptsToExecute(chatId, 2);
        historyService.setState(chatId, ActionType.AFTER_END_CHOICE);
        return messageFactory.makeTextMessage(chatId,
                textService.get("bot.info.userCreatedEndingDescription"),
                DeleteStrategy.NONE, hp);
    }


    @FSMRoute(ActionType.GET_ENDING)
    public BotMessage handleGetEnding(Update update, Long chatId, Long userId) {
        int hp = ActionType.GET_ENDING.getLifeTime();
        if (!hasCallback(update)) {
            return wrongInput(chatId, "Нужно нажать кнопку", DeleteStrategy.NONE, hp);
        }
        Event ending = historyService.getCurrentEvent(userId);
        historyService.setState(chatId, ActionType.AFTER_END_CHOICE);
        SendMessage sendMessage = new SendMessage(chatId.toString(), ending.getDescription());
        return new BotMessage(sendMessage, DeleteStrategy.DELETE_BUTTONS, hp);
    }


    @FSMRoute(ActionType.AFTER_END_CHOICE)
    public BotMessage handleAfterParty(Update update, Long chatId, Long userId) {
        log.info("AFTER_PARTY💎🔥");
        int hp = ActionType.AFTER_END_CHOICE.getLifeTime();
        return messageFactory.makeAfterEndMessage(update, chatId, userId, DeleteStrategy.NONE, hp);
    }


}
