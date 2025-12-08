package leafCatcher.service.deleteStrategy;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.Deque;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

@Service
@Slf4j
@RequiredArgsConstructor
public class DeleteMessageService {
    private final Map<Long, LastMessage> lastMessageMap = new ConcurrentHashMap<>();
    private final Map<Long, Deque<LastMessage>> allMessages = new ConcurrentHashMap<>();
    private final Map<Long, Deque<LastMessage>> waitingToDelete = new ConcurrentHashMap<>();
    private final TelegramClient telegramClient;

    public void setLestMessage(Long chatId, LastMessage lastMessage) {
        lastMessageMap.put(chatId, lastMessage);
        addToAllMessages(chatId, lastMessage);
        addToWaiting(chatId, lastMessage);
    }

    private void removeFromAllMessages(Long chatId, LastMessage lastMessage) {
        Deque<LastMessage> all = allMessages.get(chatId);
        if (all != null) {
            all.remove(lastMessage);
        }
    }

    private void removeFromWaitingMessages(Long chatId, LastMessage lastMessage) {
        Deque<LastMessage> waiting = waitingToDelete.get(chatId);
        if (waiting != null) {
            waiting.remove(lastMessage);
        }
    }

    private void addToAllMessages(Long chatId, LastMessage lastMessage) {
        if (lastMessage.getDeleteStrategy() == DeleteStrategy.NONE) {
            return;
        }
        Deque<LastMessage> dequeAllMessages = allMessages.computeIfAbsent(
                chatId,
                id -> new ConcurrentLinkedDeque<>()
        );
        dequeAllMessages.addLast(lastMessage);
    }

    private void addToWaiting(Long chatId, LastMessage lastMessage) {
        Deque<LastMessage> dequeAllMessages = waitingToDelete.computeIfAbsent(
                chatId,
                id -> new ConcurrentLinkedDeque<>()
        );
        dequeAllMessages.addLast(lastMessage);
    }

    private void deleteLastMessage(Long chatId, LastMessage lastMessage) {
        removeFromWaitingMessages(chatId, lastMessage);
        //   removeFromAllMessages(chatId, lastMessage);
        executeDelete(chatId, lastMessage);
    }


    private void decHpForWaiting(Long chatId) {
        Deque<LastMessage> messages = waitingToDelete.get(chatId);
        if (messages == null) {
            log.warn("decreaseHp не нашлось мапы messages");
            return;
        }

        //Для каждого сообщения удаляем 1 hp
        for (LastMessage lastMessage : messages) {
            lastMessage.decHp();
            if (doDeleteMessage(lastMessage, chatId)) {
                deleteLastMessage(chatId, lastMessage);
            }
        }
    }


    private String logging(LastMessage lm) {
        if (lm.getMessage().hasText()) {
            log.warn(" 💖💖💖LastMessages {}", lm.getMessage().getText());
            return lm.getMessage().getText();
        } else {
            log.warn(" LastMessages нет");
        }
        return "нет текста ";
    }

    public LastMessage getLastMessage(Long chatId) {
        return lastMessageMap.getOrDefault(chatId, null);
    }

    public boolean isMessageLast(Long chatId, LastMessage lm) {
        LastMessage last = getLastMessage(chatId);
        return last != null && last.getMessage().getMessageId().equals(lm.getMessage().getMessageId());
    }

    public boolean isPreLastMessage(Long chatId, LastMessage lm) {
        Deque<LastMessage> all = allMessages.get(chatId);
        if (all == null || all.size() < 2) return false;

        Iterator<LastMessage> it = all.descendingIterator();
        it.next();
        LastMessage preLast = it.next();

        return preLast.getMessage().getMessageId()
                .equals(lm.getMessage().getMessageId());
    }


    public void editPreviousMessage(Long chatId) {
        LastMessage lastMessage = getLastMessage(chatId);
        if (lastMessage == null) return;
        decHpForWaiting(chatId);
        DeleteStrategy deleteStrategy = lastMessage.getDeleteStrategy();
        log.warn("DeleteStrategy {}", deleteStrategy);
    }

    private void executeDelete(Long chatId, LastMessage lastMessage) {
        try {
            telegramClient.execute(new DeleteMessage(chatId.toString(), lastMessage.getMessage().getMessageId()));
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }


    private void removeInlineButtons(Long chatId, LastMessage lastMessage) {
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

    private boolean doDeleteMessage(LastMessage lastMessage, Long chatId) {
        int hp = lastMessage.getHp();
        String s = logging(lastMessage);
        log.warn("String 🔶 {}", s);

        switch (lastMessage.getDeleteStrategy()) {
            case DELETE_BUTTONS -> {
                //Удаляем кнопки, но не само сообщение;
                removeFromWaitingMessages(chatId, lastMessage);
                removeInlineButtons(chatId, lastMessage);
                return false;
            }
            case NONE -> {
                return false;
            }
            case DELETE_AFTER1 -> {
                return isPreLastMessage(chatId, lastMessage);
            }
            case DELETE_ON_NEXT -> {
                //Если данное сообщение не последнее, то удаляем. Не важно сколько hp
                return !isMessageLast(chatId, lastMessage);
            }
            case DELETE_AFTER_N_MESSAGES -> {
                log.warn("Удаляется {}", lastMessage.getMessage().getText());
                return hp < 0;
            }

        }
        log.warn("doDelete Необычное условие {}", lastMessage.getDeleteStrategy());
        return false;
    }


}
