package leafCatcher.utilityClasses.mapper;

import leafCatcher.model.Event;
import lombok.experimental.UtilityClass;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.UUID;

@UtilityClass
public class EventMapper {

    public static Event makeEvent(Update update, String description, String buttonName, boolean isEnd) {
        Event child = new Event();
        child.setTelegramId(1L);
        child.setUpdateId(0);
        child.setDescription(description);
        child.setShortName("🔷 " + buttonName);
        child.setIsRoot(false);
        child.setIsEnd(isEnd);
        child.setIsChangeable(true);
        child.setIsDummy(false);
        child.setOriginalId("NULL");
        child.setEndNumber(0);
        child.setAuthor(update.getMessage().getFrom().getUserName());

        return child;
    }

    public static Event makeDummyEvent(Update update, String buttonName, Event event) {
        Event dummy = new Event();
        dummy.setTelegramId(1L);
        dummy.setUpdateId(0);
        dummy.setDescription("description");
        dummy.setShortName("🔷 " + buttonName);
        dummy.setIsRoot(false);
        dummy.setIsEnd(event.getIsEnd());
        dummy.setIsChangeable(true);

        dummy.setIsDummy(true);
        dummy.setOriginalId(event.getElementId());

        dummy.setEndNumber(0);
        dummy.setAuthor(update.getMessage().getFrom().getUserName());

        return dummy;
    }

    public static Event getAbsentEvent() {
        Event child = new Event();
        child.setElementId("null");
        child.setTelegramId(-1L);
        child.setUpdateId(-1);
        child.setDescription("У этого события нет описания." +
                " Попробуйте создать событие заново");
        child.setShortName("Кнопка отсутствует. Sorry☹️");
        child.setIsRoot(false);
        child.setIsEnd(false);
        child.setIsChangeable(true);
        child.setIsDummy(false);
        child.setOriginalId("NULL");
        child.setEndNumber(-1);
        child.setAuthor("null");
        return child;
    }

    public static Event makeRoot(Update update, String description, String buttonName) {
        Event root = new Event();
        root.setElementId(UUID.randomUUID().toString());
        root.setTelegramId(0L);
        root.setUpdateId(0);
        root.setDescription(description);
        root.setShortName("🔶 " + buttonName);
        root.setIsRoot(true);
        root.setIsEnd(false);
        root.setIsChangeable(true);
        root.setIsDummy(false);
        root.setOriginalId("NULL");
        root.setEndNumber(0);
        root.setAuthor(update.getMessage().getFrom().getUserName());

        return root;
    }
}
