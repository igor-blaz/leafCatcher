package leafCatcher.service.deleteStrategy;

import lombok.Getter;
import lombok.NonNull;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;


@Getter
public class BotMessage {

    private final SendMessage sendMessage;
    private final DeleteStrategy deleteStrategy;

    public BotMessage(@NonNull SendMessage sendMessage,
                      @NonNull DeleteStrategy deleteStrategy) {
        this.sendMessage = sendMessage;
        this.deleteStrategy = deleteStrategy;
    }

    // удобный конструктор по умолчанию
    public BotMessage(@NonNull SendMessage sendMessage) {
        this(sendMessage, DeleteStrategy.NONE);
    }

}
