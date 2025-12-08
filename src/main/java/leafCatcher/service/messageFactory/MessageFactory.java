package leafCatcher.service.messageFactory;

import leafCatcher.history.ActionType;
import leafCatcher.history.HistoryService;
import leafCatcher.model.Event;
import leafCatcher.service.TextService;
import leafCatcher.service.deleteStrategy.BotMessage;
import leafCatcher.service.deleteStrategy.DeleteStrategy;
import leafCatcher.utilityClasses.ButtonRowDesign;
import leafCatcher.utilityClasses.GetTelegramUserName;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class MessageFactory {
    private final TextService textService;
    private final HistoryService historyService;

    public BotMessage makeMessage(long chatId,
                                  InlineKeyboardMarkup markup,
                                  String description,
                                  DeleteStrategy deleteStrategy,
                                  int hp) {
        SendMessage sendMessage = SendMessage.builder()
                .chatId(chatId)
                .text(description)
                .replyMarkup(markup)
                .build();
        return new BotMessage(sendMessage, deleteStrategy, hp);
    }

    public BotMessage makeWriteOrNotMessage(Long chatId,
                                            Event event,
                                            DeleteStrategy deleteStrategy,
                                            int hp) {
        InlineKeyboardButton iDontWant = ButtonFactory.createIDontWantWrite();
        InlineKeyboardButton iWant = ButtonFactory.createToBeContinuedButton();
        InlineKeyboardButton iWantWriteEnding = ButtonFactory.createWriteEndButton();
        InlineKeyboardButton insertFromMemory = ButtonFactory.createBondButton();
        InlineKeyboardButton putInMemory = ButtonFactory.createPutInMemoryButton();
        InlineKeyboardButton delete = ButtonFactory.createDeleteButton();

        List<InlineKeyboardRow> row = ButtonRowDesign.rowsBy2(iWant, iDontWant,
                iWantWriteEnding, putInMemory, insertFromMemory, delete);

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup(row);
        return makeMessage(chatId, markup, event.getDescription(), deleteStrategy, hp);
    }

    public BotMessage makeQuestionMessage(Update update,
                                          Long chatId,
                                          Long userId,
                                          DeleteStrategy deleteStrategy,
                                          int hp) {
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
        String name = GetTelegramUserName.getName(update);

        return makeMessage(chatId,
                markup,
                name + " ,куда пойдем дальше? ",
                deleteStrategy,
                hp);
    }

    public BotMessage makeAfterEndMessage(Update update,
                                          Long chatId,
                                          Long userId,
                                          DeleteStrategy deleteStrategy,
                                          int hp) {
        InlineKeyboardButton credits = ButtonFactory.createCreditsButton();
        InlineKeyboardButton goToStart = ButtonFactory.createStartButton();
        InlineKeyboardButton back = ButtonFactory.createGoBackButton();
        InlineKeyboardButton random = ButtonFactory.createRandomButton();
        InlineKeyboardButton putInMemory = ButtonFactory.createPutInMemoryButton();
        List<InlineKeyboardRow> keyboardRows = ButtonRowDesign.squareRow2x2PlusOne(goToStart,
                back, random, putInMemory, credits);
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup(keyboardRows);
        String name = GetTelegramUserName.getName(update);
        return makeMessage(chatId, markup,
                name + " поздравляю, тебе удалось дойти до конца", deleteStrategy, hp);
    }

    public BotMessage makeTextMessage(Long chatId,
                                      String text,
                                      DeleteStrategy strategy,
                                      int hp) {
        SendMessage sendMessage = new SendMessage(chatId.toString(), text);
        sendMessage.setParseMode(ParseMode.HTML);
        return new BotMessage(sendMessage, strategy, hp);
    }

    public BotMessage makeIDontKnowMessage(Long chatId,
                                           Long userId,
                                           DeleteStrategy deleteStrategy,
                                           int hp) {
        ActionType currentAction = historyService.getActualState(chatId);
        if (currentAction == null) {
            return makeTextMessage(chatId, textService.get("bot.help.default"), deleteStrategy, hp);
        }
        log.warn("CurrentAction {}", currentAction);
        String hint;
        switch (currentAction) {
            case START -> {
                hint = textService.getMarkdown("ru.bot.info.start");
            }
            case RANDOM -> {
                hint = "Кажется, ты нашел Пасхалку 💀🏴‍. Красава.  Можешь написать  автору о нахождении пасхалки. ️";
            }
            case GET_CHILD -> {
                hint = textService.getMarkdown("ru.bot.info.getchild");
            }
            case CREDITS -> {
                hint = textService.get("ru.bot.info.credits");
            }
            case BACK_OR_FORWARD_QUESTION -> {
                hint = textService.getMarkdown("ru.bot.info.backOrForwardHelp");
            }
            case CHILD_DESCRIPTION_CREATION -> {
                hint = textService.get("bot.help.childDescCreation");
            }
            default -> {
                hint = textService.get("bot.help.default");
            }
        }
        historyService.setAttemptsToExecute(userId, 2);
        SendMessage sendMessage = new SendMessage(chatId.toString(), hint);
        sendMessage.setParseMode(ParseMode.HTML);
        return new BotMessage(sendMessage, deleteStrategy, hp);

    }

}
