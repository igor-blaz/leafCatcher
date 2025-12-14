package leafCatcher.service.deleteStrategy.storages;

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
public class WaitingToDeleteStorage {
    private final Map<Long, List<LastMessage>> waitingToDelete = new ConcurrentHashMap<>();


    public void remove(Long chatId, LastMessage lastMessage) {
        List<LastMessage> waiting = waitingToDelete.get(chatId);
        if (waiting != null) {
            waiting.remove(lastMessage);
        }
    }

    public List<LastMessage> getWaiting(Long chatId) {
        return waitingToDelete.get(chatId);
    }


    public void addToWaiting(Long chatId, LastMessage lastMessage) {
        List<LastMessage> listAllMessages = waitingToDelete.computeIfAbsent(
                chatId,
                id -> new ArrayList<>()
        );
        listAllMessages.add(lastMessage);
    }
}
