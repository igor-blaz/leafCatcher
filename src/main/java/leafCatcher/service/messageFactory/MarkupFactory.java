package leafCatcher.service.messageFactory;

import leafCatcher.history.HistoryService;
import leafCatcher.model.Event;
import leafCatcher.utilityClasses.ButtonRowDesign;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@AllArgsConstructor
public class MarkupFactory {
    private final HistoryService historyService;


    public InlineKeyboardMarkup makeMarkup(List<Event> events, Long userId) {
        List<InlineKeyboardRow> keyboard = new ArrayList<>();
        List<InlineKeyboardRow> otherButtonsRows;
        //события, сделать продолжение или концовку
        List<InlineKeyboardRow> eventRows = makeEventRows(events);

        //Добавляем Back и Help
        InlineKeyboardButton goBack = addGoBack(userId);
        InlineKeyboardButton help = ButtonFactory.createIDontKnowButton();
        if (goBack == null) {
            otherButtonsRows = ButtonRowDesign.oneHorizontalRow(help);
        } else {
            otherButtonsRows = ButtonRowDesign.horizontal(List.of(goBack, help));
        }
        keyboard.addAll(eventRows);
        keyboard.addAll(otherButtonsRows);
        return new InlineKeyboardMarkup(keyboard);
    }

    private List<InlineKeyboardRow> makeEventRows(List<Event> events) {
        List<InlineKeyboardButton> eventsButtons = new ArrayList<>();
        for (Event event : events) {
            InlineKeyboardButton button = InlineKeyboardButton.builder()
                    .text(event.getShortName())
                    .callbackData(event.getElementId())
                    .build();
            eventsButtons.add(button);
        }
        if (events.size() < 4) {
            InlineKeyboardButton toBeContinuedButton = ButtonFactory.createToBeContinuedButton();
            InlineKeyboardButton createEnding = ButtonFactory.createWriteEndButton();
            eventsButtons.add(toBeContinuedButton);
            eventsButtons.add(createEnding);
        }
        return ButtonRowDesign.vertical(eventsButtons);
    }

    private InlineKeyboardButton addGoBack(Long userId) {
        //Если не корень
        if (!historyService.getCurrentEvent(userId).getIsRoot()) {
            return ButtonFactory.createGoBackButton();
        }
        //Если корень
        return null;
    }

    private InlineKeyboardButton addGoNext(Long userId) {
        //Если не корень
        if (!historyService.getCurrentEvent(userId).getIsEnd()) {
            return ButtonFactory.createGoBackButton();
        }
        return null;
    }
}

