package leafCatcher.handlers;

import leafCatcher.history.ActionType;
import leafCatcher.history.DraftService;
import leafCatcher.history.FSMRoute;
import leafCatcher.history.HistoryService;
import leafCatcher.service.TextService;
import leafCatcher.service.messageFactory.MarkupFactory;
import leafCatcher.service.messageFactory.MessageFactory;
import leafCatcher.storage.EventStorage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;


@Slf4j
@Component
public class WarningHandler extends AbstractFsmHandler {


    public WarningHandler(HistoryService historyService,
                          MessageFactory messageFactory,
                          MarkupFactory markupFactory,
                          EventStorage eventStorage,
                          TextService textService,
                          DraftService draftService) {
        super(historyService, messageFactory, markupFactory, eventStorage, textService, draftService);
    }

    @FSMRoute(ActionType.ADMIN_MODE)
    public SendMessage adminMode(Update update, Long chatId, Long userId) {
        log.info("AdminMode method");
        if (update.getMessage().getText().equals(super.adminCleanDb)) {
            eventStorage.killThemAll();
            historyService.setState(chatId, ActionType.START);
            historyService.setAttemptsToExecute(userId, 2);
            log.info("🔥🔥🔥🔥 База данных удалена ");

            return new SendMessage(chatId.toString(), "💎💎💎Привет, Админ. База данных очищена");
        }
        return null;
    }
}
