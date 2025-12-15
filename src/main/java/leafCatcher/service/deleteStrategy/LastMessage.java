package leafCatcher.service.deleteStrategy;

import leafCatcher.model.Event;
import lombok.Getter;
import lombok.Setter;
import org.telegram.telegrambots.meta.api.objects.message.Message;

@Getter
@Setter
public class LastMessage {
    private final Message message;
    private DeleteStrategy deleteStrategy;
    private int hp;
    private final boolean hasEvent;
    private final Event event;

    public LastMessage(Message message,
                       DeleteStrategy deleteStrategy,
                       int hp) {
        this(message, deleteStrategy, hp, null);
    }

    public LastMessage(Message message,
                       DeleteStrategy deleteStrategy,
                       int hp,
                       Event event) {

        this.message = message;
        this.deleteStrategy = deleteStrategy;
        this.hp = hp;
        this.event = event;
        this.hasEvent = event != null;
    }


    private boolean isAlive() {
        return hp > 0;
    }


    public void decHp() {
        this.hp--;
    }

}
