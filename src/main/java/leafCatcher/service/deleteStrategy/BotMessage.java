package leafCatcher.service.deleteStrategy;

import leafCatcher.model.Event;
import lombok.Getter;
import lombok.NonNull;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;


@Getter
public class BotMessage {

    private final SendMessage sendMessage;
    private final DeleteStrategy deleteStrategy;
    private final Integer hp;
    private final boolean hasEvent;
    private final Event event;


    public BotMessage(@NonNull SendMessage sendMessage,
                      @NonNull DeleteStrategy deleteStrategy,
                      Integer hp) {
        this(sendMessage, deleteStrategy, hp, null);
    }

    public BotMessage(@NonNull SendMessage sendMessage,
                      @NonNull DeleteStrategy deleteStrategy,
                      Integer hp,
                      Event event) {

        this.sendMessage = sendMessage;
        this.deleteStrategy = deleteStrategy;
        this.hp = hp;
        this.event = event;
        this.hasEvent = event != null;
    }


}
