package leafCatcher.utilityClasses;

import lombok.experimental.UtilityClass;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.Objects;

@UtilityClass
public class GetTelegramUserName {

    public static String getName(Update update) {
        String username = update.getMessage().getFrom().getUserName();
        return Objects.requireNonNullElse(username, "Игрок");
    }
}
