package leafCatcher.service.deleteStrategy;

import leafCatcher.model.Event;
import leafCatcher.service.deleteStrategy.storages.AllMessagesStorage;
import leafCatcher.service.deleteStrategy.storages.MessagesWithEventsStorage;
import leafCatcher.service.deleteStrategy.storages.WaitingToDeleteStorage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

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
    private final MessagesWithEventsStorage withEvents;
    private final ExecuteDelete executeDelete;
    private final TelegramClient telegramClient;

    public void setLestMessage(Long chatId, LastMessage lastMessage) {
        log.info("🔥🔥🔥🔥🔥");
        log.info("🔥🔥🔥🔥🔥 current {} ", lastMessage.getMessage().getText());
        current.put(chatId, lastMessage);
        allMessages.addToAllMessages(chatId, lastMessage);
        waitingMessages.addToWaiting(chatId, lastMessage);
        withEvents.addToWithEvents(chatId, lastMessage);

    }

    public void deleteFromAllStorages(Long chatId, LastMessage lastMessage) {
        waitingMessages.remove(chatId, lastMessage);
        allMessages.remove(chatId, lastMessage);
        withEvents.remove(chatId, lastMessage);
    }

    public void deleteAllChat(Long chatId) {
        List<LastMessage> all = allMessages.getAll(chatId);
        if (all.isEmpty()) return;
        for (LastMessage lastMessage : all) {
            executeDelete.execute(chatId, lastMessage);
        }
        log.info("Все сообщения у чата {} удалены", chatId);
    }

    public void deleteEventMessageFromChat(Long chatId, Event event) {
        LastMessage lastMessage = withEvents.getMessageByEvent(chatId, event);
        log.info("lastMessage is Null???? {}", lastMessage == null);
        if (lastMessage != null) {
            executeDelete.execute(chatId, lastMessage);
            // withEvents.deleteOldDuplicateEvent(chatId, event);
        }
    }

    private void deleteLastMessage(Long chatId, LastMessage lastMessage) {
        waitingMessages.remove(chatId, lastMessage);

        executeDelete.execute(chatId, lastMessage);
    }


    public void decHpForWaiting(Long chatId) {
        List<LastMessage> messages = waitingMessages.getWaiting(chatId);
        if (messages == null) {
            log.warn("decreaseHp не нашлось мапы messages");
            return;
        }
        //Для каждого сообщения удаляем 1 hp
        for (LastMessage lastMessage : messages) {
            lastMessage.decHp();
            if (doDeleteMessage(lastMessage, chatId)) {
                boolean isDeletedFromChat = executeDelete.execute(chatId, lastMessage);
                if (isDeletedFromChat) {
                    deleteFromAllStorages(chatId, lastMessage);
                }
            }
        }
    }

    public LastMessage getLastMessage(Long chatId) {
        return current.getOrDefault(chatId, null);
    }

    public boolean isMessageLast(Long chatId, LastMessage lm) {
        LastMessage last = getLastMessage(chatId);
        return last != null && last.getMessage().getMessageId().equals(lm.getMessage().getMessageId());
    }

    public boolean isPreLastMessage(Long chatId, LastMessage lm) {
        List<LastMessage> all = allMessages.getAll(chatId);
        if (all == null || all.size() < 2) return false;
        LastMessage preLast = all.get(all.size() - 2);
        return preLast.getMessage().getMessageId()
                .equals(lm.getMessage().getMessageId());
    }

    public void removeButtons(Long chatId, Message message) {
        EditMessageReplyMarkup edit = new EditMessageReplyMarkup();
        edit.setChatId(chatId.toString());
        edit.setMessageId(message.getMessageId());
        edit.setReplyMarkup(null);

        try {
            telegramClient.execute(edit);
        } catch (TelegramApiException e) {
            e.getCause();
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
        DeleteStrategy deleteStrategy = lastMessage.getDeleteStrategy();
        switch (deleteStrategy) {
            case DELETE_BUTTONS -> {
                //Удаляем кнопки, но не само сообщение;
                //lastMessage.setDeleteStrategy(DeleteStrategy.DELETE_ON_NEXT);
                removeInlineButtons(chatId, lastMessage);
                return false;
            }
            case NONE -> {
                return false;
            }
            case DELETE_AFTER_ONE -> {
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
