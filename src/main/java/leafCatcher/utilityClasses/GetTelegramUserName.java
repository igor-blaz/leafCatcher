package leafCatcher.utilityClasses;

import lombok.experimental.UtilityClass;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.Objects;

@UtilityClass
public class GetTelegramUserName {

    public static String getName(Update update) {
        if (update.hasMessage() && update.getMessage().getFrom() != null) {
            String username = update.getMessage().getFrom().getUserName();
            return username != null ? username : "Игрок💖";
        }
        if (update.hasCallbackQuery() && update.getCallbackQuery().getFrom() != null) {
            String username = update.getCallbackQuery().getFrom().getUserName();
            return username != null ? username : "Игрок💖";
        }
        return "Игрок💖";
    }

}
