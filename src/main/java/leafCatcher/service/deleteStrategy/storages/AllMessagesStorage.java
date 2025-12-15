package leafCatcher.service.deleteStrategy.storages;

import leafCatcher.service.deleteStrategy.DeleteStrategy;
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
public class AllMessagesStorage {
    private final Map<Long, List<LastMessage>> allMessages = new ConcurrentHashMap<>();



    public void remove(Long chatId, LastMessage lastMessage) {
        List<LastMessage> all = allMessages.get(chatId);
        if (all != null) {
            all.remove(lastMessage);
        }
    }
    public List<LastMessage> getAll(Long chatId){
        return allMessages.get(chatId);
    }


    public void addToAllMessages(Long chatId, LastMessage lastMessage) {
        List<LastMessage> dequeAllMessages = allMessages.computeIfAbsent(
                chatId,
                id -> new ArrayList<>()
        );
        dequeAllMessages.addLast(lastMessage);
    }
}
