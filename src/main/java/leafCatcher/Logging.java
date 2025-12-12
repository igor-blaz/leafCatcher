package leafCatcher;

import leafCatcher.service.deleteStrategy.LastMessage;
import lombok.experimental.UtilityClass;
import org.telegram.telegrambots.meta.api.objects.message.Message;

@UtilityClass
public class Logging {
    public static String getText(Message message) {
        if (message.hasText()) {
            return message.getText();
        }
        return "текста нет";
    }

    public static String getText(LastMessage lastMessage) {
        Message message = lastMessage.getMessage();
        return getText(message);
    }
}
