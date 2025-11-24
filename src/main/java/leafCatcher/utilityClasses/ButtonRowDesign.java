package leafCatcher.utilityClasses;

import lombok.experimental.UtilityClass;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;

import java.util.ArrayList;
import java.util.List;

@UtilityClass
public class ButtonRowDesign {

    public List<InlineKeyboardRow> vertical(List<InlineKeyboardButton> buttons) {
        if (buttons == null || buttons.isEmpty()) {
            return List.of();
        }
        return buttons.stream()
                .map(btn -> {
                    InlineKeyboardRow row = new InlineKeyboardRow();
                    row.add(btn);
                    return row;
                })

                .toList();
    }

    public List<InlineKeyboardRow> horizontal(List<InlineKeyboardButton> buttons) {
        if (buttons == null || buttons.isEmpty()) {
            return List.of();
        }
        InlineKeyboardRow row = new InlineKeyboardRow();
        row.addAll(buttons);
        return List.of(row);
    }

    public List<InlineKeyboardRow> oneHorizontalRow(InlineKeyboardButton button) {
        InlineKeyboardRow row = new InlineKeyboardRow();
        row.add(button);
        return List.of(row);
    }

    public List<InlineKeyboardRow> squareRow2x2(InlineKeyboardButton button1,
                                                InlineKeyboardButton button2,
                                                InlineKeyboardButton button3,
                                                InlineKeyboardButton button4) {
        List<InlineKeyboardRow> rows = new ArrayList<>();
        rows.addAll(horizontal(List.of(button1, button2)));
        rows.addAll(horizontal(List.of(button3, button4)));
        return rows;
    }

    public List<InlineKeyboardRow> squareRow2x2PlusOne(InlineKeyboardButton button1,
                                                       InlineKeyboardButton button2,
                                                       InlineKeyboardButton button3,
                                                       InlineKeyboardButton button4,
                                                       InlineKeyboardButton button5) {
        List<InlineKeyboardRow> rows = squareRow2x2(button1, button2, button3, button4);
        rows.addAll(oneHorizontalRow(button5));
        return rows;
    }

    public List<InlineKeyboardRow> twoOnTopOneBottom(InlineKeyboardButton button1,
                                                     InlineKeyboardButton button2,
                                                     InlineKeyboardButton button3
    ) {
        List<InlineKeyboardRow> rows = new ArrayList<>();
        rows.addAll(horizontal(List.of(button1, button2)));
        rows.addAll(oneHorizontalRow(button3));
        return rows;
    }


}
