package leafCatcher.service.deleteStrategy;

import lombok.Getter;
import lombok.NonNull;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;


@Getter
public class BotMessage {

    private final SendMessage sendMessage;
    private final DeleteStrategy deleteStrategy;
    private final Integer hp;

    public BotMessage(@NonNull SendMessage sendMessage,
                      @NonNull DeleteStrategy deleteStrategy,
                      Integer hp) {
        this.sendMessage = sendMessage;
        this.deleteStrategy = deleteStrategy;
        this.hp =
    }


}
