package leafCatcher.storage;

import leafCatcher.model.Event;
import leafCatcher.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Slf4j
@Repository
@RequiredArgsConstructor
public class EventStorage {
    private final EventRepository repository;

    public void deleteById(String elementId){
        repository.deleteById(elementId);
    }

    public Event getRootEvent() {
        return repository.getRoot();
    }

    public Event getParent(String eventId) {
        return repository.getParent(eventId);
    }

    public Event saveEvent(Event event) {
        return repository.save(event);
    }


    public Event saveChild(String parentElementId, Event child) {
        Event savedChild = repository.save(child);
        repository.linkParentChild(parentElementId, savedChild.getElementId());
        return savedChild;
    }

    public Event saveChildNoBack(String parentElementId, Event child) {
        Event savedChild = repository.save(child);
        repository.linkParentChildNoBack(parentElementId, savedChild.getElementId());
        return savedChild;
    }

    public Event getRandom() {
        return repository.getRandomNotEndEvent();
    }

    public Event getEventById(String eventId) {
        Optional<Event> optionalEvent = repository.findById(eventId);
        return optionalEvent.orElse(getRootEvent());
    }

    public List<Event> getChildren(String parentId) {
        return repository.getChildren(parentId);
    }

    public boolean isRootAlreadyExists() {
        return repository.existsByIsRoot(true);
    }

    //⚠️ ONLY FOR TESTING
    public void killThemAll() {
        repository.deleteAll();
    }


}
