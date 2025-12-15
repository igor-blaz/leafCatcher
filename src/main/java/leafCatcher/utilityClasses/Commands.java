package leafCatcher.utilityClasses;

import leafCatcher.history.ActionType;
import leafCatcher.history.HistoryService;
import lombok.experimental.UtilityClass;
import org.telegram.telegrambots.meta.api.objects.Update;

@UtilityClass
public class Commands {
    static final String START_COMMAND = "/start";
    static final String GET_CURRENT_COMMAND = "/current";

    public static boolean isStartCommand(Update update){
        return (update.hasMessage()) && update.getMessage().getText().equals(START_COMMAND);
    }
    public static boolean isGetCurrentCommand(Update update){
        return (update.hasMessage()) && update.getMessage().getText().equals(GET_CURRENT_COMMAND);
    }
}
