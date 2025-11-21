package leafCatcher.utilityClasses;

import lombok.experimental.UtilityClass;
import org.telegram.telegrambots.meta.api.objects.Update;

@UtilityClass
public class Commands {
    static final String START_COMMAND = "/start";

    public static boolean isStartCommand(Update update){
        return (update.hasMessage()) && update.getMessage().getText().equals(START_COMMAND);
    }
}
