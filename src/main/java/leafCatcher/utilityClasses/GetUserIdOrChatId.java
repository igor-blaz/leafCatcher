package leafCatcher.utilityClasses;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.objects.Update;

@Slf4j
@UtilityClass
public class GetUserIdOrChatId {
    public static Long getChatId(Update update) {
        if (update.hasMessage()) {
            return update.getMessage().getChatId();
        } else if (update.hasCallbackQuery()) {
            return update.getCallbackQuery().getMessage().getChatId();
        } else if (update.hasEditedMessage()) {
            return update.getEditedMessage().getChatId();
        }
        log.error("‼️‼️Не получилось получить Id Telegram чата. Будет принудительно поставлен -1");
        return -1L;
    }

    public static Long getUserId(Update update) {
        if (update.hasMessage()) {
            return update.getMessage().getFrom().getId();
        } else if (update.hasCallbackQuery()) {
            return update.getCallbackQuery().getFrom().getId();
        } else if (update.hasEditedMessage()) {
            return update.getEditedMessage().getFrom().getId();
        }
        log.error("‼️‼️Не получилось получить Id Telegram аккаунта. Будет принудительно поставлен -1");
        return -1L;
    }

    public static String getData(Update update) {
        if (update.hasCallbackQuery()) {
            return update.getCallbackQuery().getData();
        }
        return null;
    }
}
