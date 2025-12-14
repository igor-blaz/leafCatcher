package leafCatcher.service.deleteStrategy;

import leafCatcher.service.deleteStrategy.storages.AllMessagesStorage;
import leafCatcher.service.deleteStrategy.storages.WaitingToDeleteStorage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
@RequiredArgsConstructor
public class DeleteMessageService {
    private final Map<Long, LastMessage> current = new ConcurrentHashMap<>();
    private final AllMessagesStorage allMessages;
    private final WaitingToDeleteStorage waitingMessages;
    // private final MessagesWithEventsStorage withEvents;
    private final ExecuteDelete executeDelete;
    private final TelegramClient telegramClient;

    public void setLastMessage(Long chatId, LastMessage lastMessage) {
        log.info("🔥🔥🔥🔥🔥");
        log.info("🔥🔥🔥🔥🔥 current {} ", lastMessage.getMessage().getText());
        current.put(chatId, lastMessage);
        allMessages.addToAllMessages(chatId, lastMessage);
    }

    public void deleteFromAllStorages(Long chatId, LastMessage lastMessage) {
       // waitingMessages.remove(chatId, lastMessage);
        allMessages.remove(chatId, lastMessage);
        //  withEvents.remove(chatId, lastMessage);
    }

    public void deleteAllChat(Long chatId) {
        List<LastMessage> all = allMessages.getAll(chatId);
        if (all.isEmpty()) return;
        for (LastMessage lastMessage : all) {
            executeDelete.execute(chatId, lastMessage);
        }
        log.info("Все сообщения у чата {} удалены", chatId);
    }

    public void decHpForWaiting(Long chatId) {
        List<LastMessage> messages = waitingMessages.getWaiting(chatId);
        if (messages == null) return;

        List<LastMessage> toDelete = new ArrayList<>();

        for (LastMessage lastMessage : messages) {
            lastMessage.decHp();
            if (lastMessage.getHp() <= 0) {
                toDelete.add(lastMessage);
            }
        }

        for (LastMessage m : toDelete) {
            executeDelete.execute(chatId, m);
            // и ВОТ ТУТ уже можно удалить из waitingMessages (отдельным методом)
            // waitingMessages.remove(chatId, m);
        }
    }


    public LastMessage getLastMessage(Long chatId) {
        return current.getOrDefault(chatId, null);
    }

    public void removeButtons(Long chatId, LastMessage lastMessage) {
        DeleteStrategy deleteStrategy = lastMessage.getDeleteStrategy();
        log.info("deleteStrategy {}", deleteStrategy);

        switch (deleteStrategy) {
            case DELETE_ON_NEXT -> {
                executeDelete.execute(chatId, lastMessage);
                return;
            }
            case DELETE_BY_HP -> {
                if (lastMessage.getHp() <= 0) {
                    executeDelete.execute(chatId, lastMessage);
                    return;
                }
                waitingMessages.addToWaiting(chatId, lastMessage);
            }
            

        }
        EditMessageReplyMarkup edit = new EditMessageReplyMarkup();
        edit.setChatId(chatId.toString());
        edit.setMessageId(lastMessage.getMessage().getMessageId());
        edit.setReplyMarkup(null);

        try {
            telegramClient.execute(edit);
        } catch (TelegramApiException e) {
            e.getCause();
        }
    }

}
