package leafCatcher.service.messageFactory;

import leafCatcher.history.ActionType;
import lombok.experimental.UtilityClass;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;


@UtilityClass
public class ButtonFactory {
    private static final String BACK = "⏪ Назад";
    private static final String NEXT = "🔥 Далее";
    private static final String WRITE_NEXT = "🪶 Написать продолжение";
    private static final String I_WANT_WRITE_ENDING = "🔑 Написать концовку";
    private static final String I_DONT_WANT_WRITE = "🙅🚫 Не хочу писать";
    private static final String CREDITS = "💎 Конец?";
    private static final String HELP_ME = "❓Помощь";
    private static final String START = "⚡ В начало";
    private static final String RANDOM = "🧨 Случайное событие";

    public InlineKeyboardButton createRandomButton() {
        return InlineKeyboardButton.builder()
                .text(RANDOM)
                .callbackData(ActionType.RANDOM.toString())
                .build();
    }

    public InlineKeyboardButton createToBeContinuedButton() {
        return InlineKeyboardButton.builder()
                .text(WRITE_NEXT)
                .callbackData(ActionType.CHILD_DESCRIPTION_AWAIT.toString())
                .build();
    }

    public InlineKeyboardButton createIDontWantWrite() {
        return InlineKeyboardButton.builder()
                .text(I_DONT_WANT_WRITE)
                .callbackData(ActionType.GO_BACK.toString())
                .build();
    }

    public InlineKeyboardButton createGoBackButton() {
        return InlineKeyboardButton.builder()
                .text(BACK)
                .callbackData(ActionType.GO_BACK.toString())
                .build();
    }

    public InlineKeyboardButton createGoNextButton() {
        return InlineKeyboardButton.builder()
                .text(NEXT)
                .callbackData(ActionType.GET_CHILD.toString())
                .build();
    }

    public InlineKeyboardButton createWriteEndButton() {
        return InlineKeyboardButton.builder()
                .text(I_WANT_WRITE_ENDING)
                .callbackData(ActionType.END_IS_ABSENCE_INFO.toString())
                .build();
    }


    public InlineKeyboardButton createCreditsButton() {
        return InlineKeyboardButton.builder()
                .text(CREDITS)
                .callbackData(ActionType.CREDITS.toString())
                .build();
    }

    public InlineKeyboardButton createIDontKnowButton() {
        return InlineKeyboardButton.builder()
                .text(HELP_ME)
                .callbackData(ActionType.I_DONT_KNOW.toString())
                .build();
    }

    public InlineKeyboardButton createStartButton() {
        return InlineKeyboardButton.builder()
                .text(START)
                .callbackData(ActionType.START.toString())
                .build();
    }


}
