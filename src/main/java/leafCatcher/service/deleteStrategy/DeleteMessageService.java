package leafCatcher.service.deleteStrategy;

import leafCatcher.history.ActionType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

@Service
@Slf4j
@RequiredArgsConstructor
public class DeleteMessageService {
    private final Map<Long, LastMessage> lastMessageMap = new ConcurrentHashMap<>();
    private final Map<Long, Deque<LastMessage>> allMessages = new ConcurrentHashMap<>();
    private final Map<Long,  Deque<LastMessage>> waitingToDelete = new ConcurrentHashMap<>();
    private final TelegramClient telegramClient;

    public void setLestMessage(Long chatId, LastMessage lastMessage) {
        lastMessageMap.put(chatId, lastMessage);
        addToAllMessages(chatId, lastMessage);
        addToWaiting(chatId, lastMessage);

    }
    private void addToAllMessages(Long chatId, LastMessage lastMessage){
        Deque<LastMessage> dequeAllMessages = allMessages.computeIfAbsent(
                chatId,
                id -> new ConcurrentLinkedDeque<>()
        );
        dequeAllMessages.addLast(lastMessage);
    }
    private void addToWaiting(Long chatId, LastMessage lastMessage){
        Deque<LastMessage> dequeAllMessages = waitingToDelete.computeIfAbsent(
                chatId,
                id -> new ConcurrentLinkedDeque<>()
        );
        dequeAllMessages.addLast(lastMessage);
    }

    public LastMessage getLestMessage(Long chatId) {
        return lastMessageMap.getOrDefault(chatId, null);
    }

    public void editPreviousMessage(Long chatId) {
        LastMessage lastMessage = getLestMessage(chatId);
        if (chatId == null || lastMessage == null) {
            log.info("chat ID {} , lastMessage {}", chatId, lastMessage);
            return;
        }
        DeleteStrategy deleteStrategy = lastMessage.getDeleteStrategy();
        Message message = lastMessage.getMessage();
        if (deleteStrategy == null || message == null) {
            log.info("deleteStrategy  {} , message {}", deleteStrategy, message);
            return;
        }
        int messageId = message.getMessageId();
        log.warn("DeleteStrategy {}", deleteStrategy);
        switch (deleteStrategy) {

            case DELETE_BUTTONS-> removeInlineButtons(chatId, messageId);

            case DELETE_ON_NEXT -> deleteMessage(chatId, messageId);
        }

    }

    private void deleteMessage(Long chatId, int messageId) {
        try {
            log.info("DELETE");
            telegramClient.execute(new DeleteMessage(chatId.toString(), messageId));
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }


    private void removeInlineButtons(Long chatId, int messageId) {
        EditMessageReplyMarkup edit = new EditMessageReplyMarkup();
        edit.setChatId(chatId.toString());
        edit.setMessageId(messageId);
        edit.setReplyMarkup(null);

        try {
            telegramClient.execute(edit);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }


}
