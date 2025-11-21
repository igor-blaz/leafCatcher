package leafCatcher;

import leafCatcher.history.ActionType;
import leafCatcher.history.HistoryService;
import leafCatcher.service.EventMainService;
import leafCatcher.utilityClasses.GetUserIdOrChatId;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;


@Slf4j
@AllArgsConstructor
public class LeafCatcher implements LongPollingSingleThreadUpdateConsumer {
    private final TelegramClient telegramClient;
    private final EventMainService eventMainService;
    private final HistoryService historyService;

    public static final String ADMIN_CLEAN_DB = "Пикачуdltt";

    @Override
    public void consume(Update update) {
        SendMessage sendMessage;
        Long chatId = GetUserIdOrChatId.getChatId(update);
        Long userId = GetUserIdOrChatId.getUserId(update);

        if (update.hasMessage() && update.getMessage().hasText() &&
                !update.hasCallbackQuery()) {
            sendMessage = sendMessageByText(update, chatId, userId);

        } else if (update.hasCallbackQuery()) {
            sendMessage = sendMessageByCallback(update, chatId, userId);
        } else {
            sendMessage = new SendMessage(chatId.toString(), "Кажется, это ошибка");
        }
        ActionType state = historyService.getActualState(chatId);
        log.warn("🥵consume {}", state);
        executeMessage(sendMessage);
        repeatConsume(update, chatId, userId);
    }

    private SendMessage sendMessageByText(Update update, Long chatId, Long userId) {
        if (update.getMessage().getText().equals(ADMIN_CLEAN_DB)) {
            log.error("Admin mode включен");
            historyService.setState(chatId, ActionType.ADMIN_MODE);
            historyService.setAttemptsToExecute(userId, 2);
        }
        return eventMainService.makeMessageByText(update, chatId, userId);
    }

    public SendMessage sendMessageByCallback(Update update, Long chatId, Long userId) {
        log.info("HAS CALLBACK");
        return eventMainService.makeMessageByCallback(update, chatId, userId);
    }

    public void repeatConsume(Update update, Long chatId, Long userId) {
        int attempts = historyService.getAttemptsToExecute(userId);
        if (attempts <= 1) {
            return;
        }
        ActionType state = historyService.getActualState(chatId);
        log.warn("🥵repeatConsume {}", state);
        SendMessage second = eventMainService.makeMessageByText(update, chatId, userId);
        executeMessage(second);

        historyService.setZeroAttempts(userId);
    }


    public void executeMessage(SendMessage message) {

        try {
            telegramClient.execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }


}
