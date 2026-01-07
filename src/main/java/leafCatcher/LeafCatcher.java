package leafCatcher;

import leafCatcher.history.ActionType;
import leafCatcher.history.HistoryService;
import leafCatcher.service.EventMainService;
import leafCatcher.service.deleteStrategy.BotMessage;
import leafCatcher.service.deleteStrategy.DeleteMessageService;
import leafCatcher.service.deleteStrategy.DeleteStrategy;
import leafCatcher.service.deleteStrategy.LastMessage;
import leafCatcher.utilityClasses.GetUserIdOrChatId;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.List;


@Slf4j
@AllArgsConstructor
public class LeafCatcher implements LongPollingSingleThreadUpdateConsumer {
    private final TelegramClient telegramClient;
    private final EventMainService eventMainService;
    private final HistoryService historyService;
    private final DeleteMessageService deleteMessageService;

    @Value("${admin.secret.command.cleanNeo4j}")
    private String adminCleanDb;


    @Override
    public void consume(Update update) {

        log.info(">>> UPDATE RECEIVED: id={}, date={}, hasMessage={}",
                update.getUpdateId(),
                update.getMessage() != null ? update.getMessage().getDate() : null,
                update.hasMessage());

        BotMessage botMessage;
        Long chatId = GetUserIdOrChatId.getChatId(update);
        Long userId = GetUserIdOrChatId.getUserId(update);

        if (update.hasMessage() && update.getMessage().hasText() &&
                !update.hasCallbackQuery()) {
            botMessage = sendMessageByText(update, chatId, userId);

        } else if (update.hasCallbackQuery()) {
            botMessage = sendMessageByCallback(update, chatId, userId);
        } else {
            SendMessage sendMessage = new SendMessage(chatId.toString(), "Кажется, это ошибка");
            botMessage = new BotMessage(sendMessage, DeleteStrategy.DELETE_ON_NEXT, 0);

        }
        ActionType state = historyService.getActualState(chatId);
        log.warn("🥵consume {}", state);
        executeMessage(botMessage, chatId);
        repeatConsume(update, chatId, userId);
    }

    private BotMessage sendMessageByText(Update update, Long chatId, Long userId) {
        if (update.getMessage().getText().equals(adminCleanDb)) {
            log.error("Admin mode включен");
            historyService.setState(chatId, ActionType.ADMIN_MODE);
            historyService.setAttemptsToExecute(userId, 2);
        }
        return eventMainService.makeMessageByText(update, chatId, userId);
    }

    public BotMessage sendMessageByCallback(Update update, Long chatId, Long userId) {
        return eventMainService.makeMessageByCallback(update, chatId, userId);
    }

    public void repeatConsume(Update update, Long chatId, Long userId) {
        int attempts = historyService.getAttemptsToExecute(userId);
        if (attempts <= 1) {
            return;
        }

        ActionType state = historyService.getActualState(chatId);
        log.warn("repeatConsume {}", state);
        BotMessage second = eventMainService.makeMessageByText(update, chatId, userId);
        executeMessage(second, chatId);

        historyService.setZeroAttempts(userId);

    }


    public void executeMessage(BotMessage botMessage, Long chatId) {
        try {
            // 1) СНАЧАЛА чистим прошлое активное сообщение
            LastMessage prev = deleteMessageService.getLastMessage(chatId);
            if (prev != null && prev.getMessage() != null) {
                deleteMessageService.removeButtons(chatId, prev);
            }

            // 2) Потом отправляем новое
            Message message = telegramClient.execute(botMessage.getSendMessage());
            deleteMessageService.decHpForWaiting(chatId);

            // 3) И сохраняем его как новое lastMessage
            LastMessage lastMessage = new LastMessage(message,
                    botMessage.getDeleteStrategy(),
                    botMessage.getHp(), botMessage.getEvent());
            deleteMessageService.setLastMessage(chatId, lastMessage);


        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }


}
