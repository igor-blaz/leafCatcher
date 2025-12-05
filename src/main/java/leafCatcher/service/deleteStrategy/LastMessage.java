package leafCatcher.service.deleteStrategy;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.telegram.telegrambots.meta.api.objects.message.Message;

@Getter
@AllArgsConstructor
public class LastMessage {
    private Message message;
    private DeleteStrategy deleteStrategy;
}
