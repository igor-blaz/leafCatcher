package leafCatcher.history;

import leafCatcher.model.Event;
import leafCatcher.utilityClasses.mapper.EventMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

@Slf4j
@Service
public class HistoryService {
    private final Map<Long, Deque<ActionType>> history = new ConcurrentHashMap<>();
    private final Map<Long, Integer> attemptsToExecute = new ConcurrentHashMap<>();
    private final Map<Long, Event> currentEvent = new ConcurrentHashMap<>();
    private final Map<Long, Boolean> skipForcedStart = new ConcurrentHashMap<>();

    public void setSkipStart(Long userId) {
        skipForcedStart.put(userId, true);
    }

    public void doNotSkipStart(Long userId) {
        skipForcedStart.put(userId, false);
    }

    public boolean isSkipStart(Long userId) {
        boolean skip = skipForcedStart.getOrDefault(userId, false);
        //doNotSkipStart(userId);
        return skip;
    }

    public void setCurrentEvent(Long userId, Event event) {
        log.info("✅✅ Current Event {}", event.getShortName());
        currentEvent.put(userId, event);
    }

    public Event getCurrentEvent(Long userId) {
        return currentEvent.getOrDefault(userId, EventMapper.getAbsentEvent());
    }


    //Attempts == сколько сообщений придет за один Update
    //если поставить 3, то одним сообщением можно затриггерить 3 Execute
    public void setAttemptsToExecute(Long userId, Integer attempts) {
        attemptsToExecute.put(userId, attempts);
    }

    public void setZeroAttempts(Long userId) {
        attemptsToExecute.put(userId, 0);
    }

    public Integer getAttemptsToExecute(Long userId) {
        return attemptsToExecute.getOrDefault(userId, 0);
    }

    public ActionType getActualState(long chatId) {
        Deque<ActionType> deque = history.get(chatId);
        if (deque == null || deque.isEmpty()) {
            return ActionType.START;
        }
        return deque.peekLast();
    }

    public void reset(Long chatId, Long userId) {
        history.put(chatId, new ArrayDeque<>());
        setAttemptsToExecute(userId, 2);
        log.info("Reset completed for chatId={}", chatId);
    }

    public boolean isNewbiePlayer(Long chatId) {
        int size = history.get(chatId).size();
        log.info("история игрока {}", history.get(chatId));
        System.out.println(history.get(chatId));
        return size <= 2;
    }


    public void setState(long chatId, ActionType state) {
        Deque<ActionType> deque = history.computeIfAbsent(
                chatId,
                id -> new ConcurrentLinkedDeque<>()
        );
        deque.addLast(state);
        log.debug("chat {} -> {}", chatId, state);
    }


}
