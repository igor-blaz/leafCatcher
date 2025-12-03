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
    public SendMessage handleNoEndInfo(Update update, Long chatId, Long userId) {
        //1. Уведомляем, что концовки нет. Переходим в состояние создания кнопки
        historyService.setState(chatId, ActionType.ENDING_BUTTON_CREATION);
        return new SendMessage(
                chatId.toString(),
                textService.get("bot.info.userWantsCreateEnd")
        );
    }

    @FSMRoute(ActionType.ENDING_BUTTON_CREATION)
    public SendMessage handleRootButton(Update update, Long chatId, Long userId) {
        SendMessage reject = rejectCallbackWhenExpectingText(update, chatId, "текст описания события");
        if (reject != null) {
            return reject;
        }
        if (!hasText(update)) {
            return wrongInput(chatId, "текст описания события");
        }

        String button = update.getMessage().getText();
        draftService.setEndingButtonName(userId, button);
        historyService.setState(chatId, ActionType.ENDING_DESCRIPTION_CREATION);

        return new SendMessage(chatId.toString(),
                textService.get("bot.info.endingButtonCreation"));
    }

    @FSMRoute(ActionType.ENDING_DESCRIPTION_CREATION)
    public SendMessage handleEndDescription(Update update, Long chatId, Long userId) {
        SendMessage reject = rejectCallbackWhenExpectingText(update, chatId, "текст описания события");
        if (reject != null) {
            return reject;
        }
        if (!hasText(update)) {
            return wrongInput(chatId, "текст описания события");
        }


        String description = update.getMessage().getText();
        String buttonName = draftService.getEndingButtonName(userId);
        Event parent = historyService.getCurrentEvent(userId);
        if (parent == null) {
            return new SendMessage(chatId.toString(),
                    "Не могу создать кнопку: не найден родительский лист 🥲");
        }
        Event ending = EventMapper.makeEvent(update, description, buttonName, true);
        eventStorage.saveChild(parent.getElementId(), ending);
        historyService.setCurrentEvent(chatId, ending);
        historyService.setAttemptsToExecute(chatId, 2);
        historyService.setState(chatId, ActionType.AFTER_END_CHOICE);
        return new SendMessage(
                chatId.toString(),
                textService.get("bot.info.userCreatedEndingDescription")
        );
    }


    @FSMRoute(ActionType.GET_ENDING)
    public SendMessage handleGetEnding(Update update, Long chatId, Long userId) {
        if (!hasCallback(update)) {
            return wrongInput(chatId, "Нужно нажать кнопку");
        }
        Event ending = historyService.getCurrentEvent(userId);
        historyService.setState(chatId, ActionType.AFTER_END_CHOICE);
        return new SendMessage(chatId.toString(), ending.getDescription());
    }


    @FSMRoute(ActionType.AFTER_END_CHOICE)
    public SendMessage handleAfterParty(Update update, Long chatId, Long userId) {
        log.info("AFTER_PARTY💎🔥");
        return messageFactory.makeAfterEndMessage(update, chatId, userId);
    }


}
