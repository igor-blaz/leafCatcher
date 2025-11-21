package leafCatcher.service.messageFactory;

import leafCatcher.history.ActionType;
import leafCatcher.history.HistoryService;
import leafCatcher.model.Event;
import leafCatcher.service.MessageService;
import leafCatcher.storage.EventStorage;
import leafCatcher.utilityClasses.ButtonRowDesign;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class MessageFactory {
    private final MessageService messageService;
    private final EventStorage storage;
    private final MarkupFactory markupFactory;
    private final HistoryService historyService;

    public SendMessage makeMessage(long chatId,
                                   InlineKeyboardMarkup markup,
                                   String description) {
        return SendMessage.builder()
                .chatId(chatId)
                .text(description)
                .replyMarkup(markup)
                .build();
    }

    public SendMessage makeWriteOrNotEnding(Long chatId) {
        InlineKeyboardButton iDontWant = ButtonFactory.createIDontWantWrite();
        InlineKeyboardButton help = ButtonFactory.createIDontKnowButton();

        List<InlineKeyboardRow> row = ButtonRowDesign.horizontal(List.of(iDontWant, help));

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup(row);
        return makeMessage(chatId, markup, messageService.get("bot.info.thereIsNoChildEvent"));
    }

    public SendMessage makeWriteOrNotMessage(Long chatId, Event event) {
        InlineKeyboardButton iDontWant = ButtonFactory.createIDontWantWrite();
        InlineKeyboardButton iWant = ButtonFactory.createToBeContinuedButton();
        InlineKeyboardButton iWantWriteEnding = ButtonFactory.createWriteEndButton();

        List<InlineKeyboardRow> row = ButtonRowDesign.twoOnTopOneBottom(iWant, iDontWant, iWantWriteEnding);

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup(row);
        return makeMessage(chatId, markup, event.getDescription());
    }

    public SendMessage makeQuestionMessage(Long chatId, Long userId) {
        InlineKeyboardButton goBack = ButtonFactory.createGoBackButton();
        InlineKeyboardButton goNext = ButtonFactory.createGoNextButton();
        InlineKeyboardRow row = new InlineKeyboardRow();
        row.add(goNext);
        if (!historyService.getCurrentEvent(userId).getIsRoot()) {
            row.add(goBack);
        }
        row.add(ButtonFactory.createIDontKnowButton());
        List<InlineKeyboardRow> keyboard = List.of(row);
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup(keyboard);

        return makeMessage(chatId, markup, "Куда пойдем дальше? ");
    }

    public SendMessage makeAfterEndMessage(Long chatId, Long userId) {
        InlineKeyboardButton credits = ButtonFactory.createCreditsButton();
        InlineKeyboardButton goToStart = ButtonFactory.createStartButton();
        InlineKeyboardButton back = ButtonFactory.createGoBackButton();
        InlineKeyboardButton random = ButtonFactory.createRandomButton();
        List<InlineKeyboardRow> keyboardRows = ButtonRowDesign.twoOnTopOneBottom(goToStart, back, credits);
        keyboardRows.add(new InlineKeyboardRow(random));
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup(keyboardRows);
        return makeMessage(chatId, markup, "Поздравляю, вы прошли игру");
    }

    public SendMessage makeTextMessage(Long chatId, String text) {
        SendMessage sendMessage = new SendMessage(chatId.toString(), text);
        sendMessage.setParseMode(ParseMode.HTML);
        return sendMessage;
    }

    public SendMessage makeIDontKnowMessage(Long chatId, Long userId) {
        ActionType currentAction = historyService.getActualState(chatId);
        if (currentAction == null) {
            return makeTextMessage(chatId, messageService.get("bot.help.default"));
        }
        log.warn("CurrentAction {}", currentAction);
        String hint;
        switch (currentAction) {
            case START -> {
                hint = messageService.getMarkdown("ru.bot.info.start");
            }
            case RANDOM -> {
                hint = "Кажется, ты нашел Пасхалку 💀🏴‍. Красава.  Можешь написать  автору о нахождении пасхалки. ️";
            }
            case GET_CHILD -> {
                hint = messageService.getMarkdown("ru.bot.info.getchild");
            }
            case CREDITS -> {
                hint = messageService.get("ru.bot.info.credits");
            }
            case BACK_OR_FORWARD_QUESTION -> {
                hint = messageService.getMarkdown("ru.bot.info.backOrForwardHelp");
            }
            case CHILD_DESCRIPTION_CREATION -> {
                hint = messageService.get("bot.help.childDescCreation");
            }
            default -> {
                hint = messageService.get("bot.help.default");
            }
        }
        historyService.setAttemptsToExecute(userId, 2);
        SendMessage sendMessage = new SendMessage(chatId.toString(), hint);
        sendMessage.setParseMode(ParseMode.HTML);
        return sendMessage;

    }

}
