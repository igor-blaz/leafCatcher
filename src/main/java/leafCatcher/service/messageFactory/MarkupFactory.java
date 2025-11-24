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
    private static final Integer MAX_EVENTS_FOR_CHILD = 4;


    public InlineKeyboardMarkup makeMarkup(List<Event> events, Long userId) {
        List<InlineKeyboardRow> keyboard = new ArrayList<>();
        List<InlineKeyboardRow> otherButtonsRows;

        //события, сделать продолжение или концовку
        List<InlineKeyboardRow> eventRows = makeEventRows(events, userId);
        List<InlineKeyboardRow> actionRows = makeActionRows(eventRows.size(), userId);

        //Добавляем Back и Help
        keyboard.addAll(eventRows);
        keyboard.addAll(actionRows);
        InlineKeyboardButton goBack = addGoBack(userId);
        if (goBack != null) {
            otherButtonsRows = ButtonRowDesign.oneHorizontalRow(goBack);
            keyboard.addAll(otherButtonsRows);
        }


        return new InlineKeyboardMarkup(keyboard);
    }

    private List<InlineKeyboardRow> makeEventRows(List<Event> events, Long userId) {
        List<InlineKeyboardButton> eventsButtons = new ArrayList<>();
        for (Event event : events) {
            InlineKeyboardButton button = InlineKeyboardButton.builder()
                    .text(event.getShortName())
                    .callbackData(event.getElementId())
                    .build();
            eventsButtons.add(button);
        }
        return ButtonRowDesign.vertical(eventsButtons);
    }


    private List<InlineKeyboardRow> makeActionRows(int eventsSize, Long userId) {
        List<InlineKeyboardButton> actionButtons = new ArrayList<>();
        //Кнопка запомнить
        InlineKeyboardButton putInMemory = ButtonFactory.putInMemory();
        actionButtons.add(putInMemory);
        //Помощь
        InlineKeyboardButton help = ButtonFactory.createIDontKnowButton();
        actionButtons.add(help);

        //Если есть, что добавить ставим (написать концовку написать продолжение  или связать)
        if (eventsSize < MAX_EVENTS_FOR_CHILD) {
            //Написать продолжение
            InlineKeyboardButton toBeContinuedButton = ButtonFactory.createToBeContinuedButton();

            // Написать концовку
            InlineKeyboardButton createEnding = ButtonFactory.createWriteEndButton();

            // Связать с памятью
            InlineKeyboardButton bond = ButtonFactory.showMemory();

            return ButtonRowDesign.squareRow2x2PlusOne(toBeContinuedButton,
                    createEnding, help, putInMemory, bond);

        }


        return ButtonRowDesign.horizontal(actionButtons);
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