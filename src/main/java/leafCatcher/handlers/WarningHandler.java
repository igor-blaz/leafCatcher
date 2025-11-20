package leafCatcher.handlers;

import leafCatcher.history.ActionType;
import leafCatcher.history.DraftService;
import leafCatcher.history.FSMRoute;
import leafCatcher.history.HistoryService;
import leafCatcher.service.MessageService;
import leafCatcher.service.messageFactory.MarkupFactory;
import leafCatcher.service.messageFactory.MessageFactory;
import leafCatcher.storage.EventStorage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import static leafCatcher.LeafCatcher.ADMIN_CLEAN_DB;

@Slf4j
@Component
public class WarningHandler extends AbstractFsmHandler {

    public WarningHandler(HistoryService historyService,
                          MessageFactory messageFactory,
                          MarkupFactory markupFactory,
                          EventStorage eventStorage,
                          MessageService messageService,
                          DraftService draftService) {
        super(historyService, messageFactory, markupFactory, eventStorage, messageService, draftService);
    }

    @FSMRoute(ActionType.ADMIN_MODE)
    public SendMessage adminMode(Update update, Long chatId, Long userId) {
        log.info("AdminMode method");
        if (update.getMessage().getText().equals(ADMIN_CLEAN_DB)) {
            eventStorage.killThemAll();
            historyService.setState(chatId, ActionType.START);
            historyService.setAttemptsToExecute(userId, 2);
            log.info("🔥🔥🔥🔥 База данных удалена ");

            return new SendMessage(chatId.toString(), "💎💎💎Привет, Админ. База данных очищена");
        }
        return null;
    }
}
