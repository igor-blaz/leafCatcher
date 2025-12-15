package leafCatcher.service.deleteStrategy;

import leafCatcher.Logging;
import leafCatcher.service.deleteStrategy.storages.AllMessagesStorage;
import leafCatcher.service.deleteStrategy.storages.WaitingToDeleteStorage;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

@Service
@RequiredArgsConstructor
public class ExecuteDelete {

    private static final Logger log = LoggerFactory.getLogger(ExecuteDelete.class);
    private final TelegramClient telegramClient;
    private final WaitingToDeleteStorage waitingToDeleteStorage;
    private final AllMessagesStorage allMessagesStorage;


    public boolean execute(Long chatId, LastMessage lastMessage) {
        try {
            telegramClient.execute(new DeleteMessage(chatId.toString(), lastMessage.getMessage().getMessageId()));
            deleteFromAllMaps(chatId, lastMessage);
            return true;
        } catch (TelegramApiException e) {
            log.error("Не получилось удалить {}", Logging.getText(lastMessage));
            e.getCause();
            return false;
        }
    }

    private void deleteFromAllMaps(Long chatId, LastMessage lastMessage) {
        waitingToDeleteStorage.remove(chatId, lastMessage);
        allMessagesStorage.remove(chatId, lastMessage);
    }
}
