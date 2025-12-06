package leafCatcher.service.deleteStrategy;

import lombok.Getter;
import org.telegram.telegrambots.meta.api.objects.message.Message;

@Getter
public class LastMessage {
    private final Message message;
    private final DeleteStrategy deleteStrategy;
    private int hp;

    public LastMessage(Message message, DeleteStrategy deleteStrategy, int hp) {
        this.message = message;
        this.deleteStrategy = deleteStrategy;
        this.hp = hp;
    }

    private int setHp() {
        return switch (deleteStrategy) {
            case DELETE_ON_NEXT -> 1;
            default -> 0;
        };
    }

    private boolean setEditAfter() {
        return hp > 0;
    }


    public void decHp() {
        this.hp--;
    }

}
