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
        List<InlineKeyboardRow> backAndActions = backAndActions();


        //Добавляем Back и Help
        keyboard.addAll(eventRows);
        keyboard.addAll(backAndActions);
        InlineKeyboardButton goBack = addGoBack(userId);
        if (goBack != null) {
            otherButtonsRows = ButtonRowDesign.oneHorizontalRow(goBack);
            keyboard.addAll(otherButtonsRows);
        }


        return new InlineKeyboardMarkup(keyboard);
    }

    public InlineKeyboardMarkup makeActionMarkup(int size, Long userId, Event current) {
        List<InlineKeyboardRow> actions = makeActionRows(size, userId, current);
        List<InlineKeyboardRow> keyboard = new ArrayList<>(actions);
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

    private List<InlineKeyboardRow> backAndActions() {
        //  InlineKeyboardButton goBack = ButtonFactory.createGoBackButton();
        InlineKeyboardButton actions = ButtonFactory.createActionButton();
        return ButtonRowDesign.oneHorizontalRow(actions);
    }


    private List<InlineKeyboardRow> makeActionRows(int eventsSize, Long userId, Event current) {
        List<InlineKeyboardButton> actionButtons = new ArrayList<>();
        //Кнопка запомнить
        InlineKeyboardButton putInMemory = ButtonFactory.createPutInMemoryButton();
        actionButtons.add(putInMemory);
        //Помощь
        InlineKeyboardButton help = ButtonFactory.createIDontKnowButton();
        actionButtons.add(help);

        //Удалить
        InlineKeyboardButton delete = ButtonFactory.createDeleteButton();
        actionButtons.add(delete);


        log.info("CURRENT {}", current);
        InlineKeyboardButton back = ButtonFactory.createRepeatCurrentEventButton();


        //Если есть, что добавить ставим (написать концовку написать продолжение  или связать)
        if (eventsSize < MAX_EVENTS_FOR_CHILD) {
            //Написать продолжение
            InlineKeyboardButton toBeContinuedButton = ButtonFactory.createToBeContinuedButton();

            // Написать концовку
            InlineKeyboardButton createEnding = ButtonFactory.createWriteEndButton();
            if (historyService.showMemory(userId) != null) {
                InlineKeyboardButton bond = ButtonFactory.createBondButton();
                return ButtonRowDesign.rowsBy2(toBeContinuedButton,
                        createEnding, help, putInMemory, bond, back, delete);
            }
            return ButtonRowDesign.rowsBy2(toBeContinuedButton,
                    createEnding, help, putInMemory, back, delete);

        }
        actionButtons.add(back);
        return ButtonRowDesign.squareRow2x2(help, putInMemory, back, delete);
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