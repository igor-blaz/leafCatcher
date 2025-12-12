package leafCatcher.service.deleteStrategy.storages;

import leafCatcher.model.Event;
import leafCatcher.service.deleteStrategy.ExecuteDelete;
import leafCatcher.service.deleteStrategy.LastMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
@RequiredArgsConstructor
public class MessagesWithEventsStorage {
    private final Map<Long, List<LastMessage>> withEvents = new ConcurrentHashMap<>();
    private final ExecuteDelete executeDelete;

    public void addToWithEvents(Long chatId, LastMessage lastMessage) {
        if (!lastMessage.isHasEvent()) return;
        if (lastMessage.getMessage() != null) {
            List<LastMessage> messageList = withEvents.computeIfAbsent(
                    chatId,
                    id -> new ArrayList<>()
            );
            log.error("🧠🧠🧠🧠Добавлено событие {}", lastMessage.getMessage().getText());
            messageList.add(lastMessage);
        }
    }

    public void remove(Long chatId, LastMessage lastMessage) {
        List<LastMessage> waiting = withEvents.get(chatId);
        if (waiting != null) {
            waiting.remove(lastMessage);
        }
    }

    public void deleteOldDuplicateEvent(Long chatId, Event event) {
        List<LastMessage> messageList = withEvents.get(chatId);
        if (messageList == null || messageList.isEmpty()) return;
        for (LastMessage lastMessage : messageList) {
            if (lastMessage.getEvent().getShortName().equals(event.getShortName())) {
                executeDelete.execute(lastMessage.getMessage().getChatId(), lastMessage);
                return;
            }
        }
    }


    public LastMessage getMessageByEvent(Long chatId, Event event) {
        List<LastMessage> messageList = withEvents.get(chatId);
        if (messageList == null) {
            log.info("Не нашлось сообщения с событием в чате");
            return null;
        }
        for (LastMessage lastMessage : messageList) {

            if (lastMessage.getEvent() != null) {
                if (lastMessage.getEvent().getShortName().equals(event.getShortName())) {
                    return lastMessage;
                }
            }
        }
        log.info("Не нашлось сообщения с событием в чате");
        return null;
    }
}
