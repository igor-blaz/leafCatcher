package leafCatcher.handlers;

import leafCatcher.history.ActionType;
import leafCatcher.history.DraftService;
import leafCatcher.history.FSMRoute;
import leafCatcher.history.HistoryService;
import leafCatcher.model.Event;
import leafCatcher.service.TextService;
import leafCatcher.service.deleteStrategy.BotMessage;
import leafCatcher.service.deleteStrategy.DeleteStrategy;
import leafCatcher.service.messageFactory.MarkupFactory;
import leafCatcher.service.messageFactory.MessageFactory;
import leafCatcher.storage.EventStorage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
@Slf4j
public class MemoryHandler extends AbstractFsmHandler {
    public MemoryHandler(HistoryService historyService,
                         MessageFactory messageFactory,
                         MarkupFactory markupFactory,
                         EventStorage eventStorage,
                         TextService textService,
                         DraftService draftService) {
        super(historyService, messageFactory, markupFactory, eventStorage, textService, draftService);
    }

    @FSMRoute(ActionType.PUT_IN_MEMORY)
    public BotMessage putInMemory(Update update, Long chatId, Long userId) {
        int hp = ActionType.PUT_IN_MEMORY.getLifeTime();
        DeleteStrategy deleteStrategy = ActionType.PUT_IN_MEMORY.getDeleteStrategy();
        log.info("Put in memory");
        historyService.setState(chatId, ActionType.REPEAT_CURRENT);
        historyService.setAttemptsToExecute(userId, 2);
        Event current = historyService.getCurrentEvent(userId);
        if (current == null) {
            return messageFactory.makeTextMessage(chatId,
                    "Сейчас нет события, которое можно запомнить.",
                    DeleteStrategy.DELETE_BY_HP, hp);
        }

        historyService.addInMemory(userId, current);
        if(current.getIsEnd()){
            return messageFactory.makeTextMessage(chatId,
                    "💾 Отлично, концовка " + current.getShortName() + " сохранена 🔑",
                    deleteStrategy, hp);
        }
        return messageFactory.makeTextMessage(chatId,
                "💾 Отлично, событие " + current.getShortName() + " сохранено 🔥",
                deleteStrategy, hp);
    }

    @FSMRoute(ActionType.BOND)
    public BotMessage showMemory(Update update, Long chatId, Long userId) {
        int hp = ActionType.BOND.getLifeTime();
        DeleteStrategy deleteStrategy = ActionType.BOND.getDeleteStrategy();
        log.info("Show memory");
        historyService.setAttemptsToExecute(userId, 2);
        Event memoryEvent = historyService.showMemory(userId);
        if (memoryEvent == null) {
            return messageFactory.makeTextMessage(chatId,
                    "У вас в памяти ноль событий",
                    DeleteStrategy.DELETE_BY_HP, hp);
        }

        Event parent = historyService.getCurrentEvent(userId);
        if (parent == null) {
            return messageFactory.makeTextMessage(chatId,
                    "Сейчас нет события, к которому можно привязать событие из памяти.",
                    DeleteStrategy.DELETE_ON_NEXT, hp);
        }

        if (parent.getElementId().equals(memoryEvent.getElementId())) {
            return messageFactory.makeTextMessage(chatId,
                    "Нельзя привязать событие само к себе 🙂",
                    DeleteStrategy.DELETE_ON_NEXT, hp);
        }

        eventStorage.saveChildNoBack(parent.getElementId(), memoryEvent);

        return messageFactory.makeTextMessage(
                chatId,
                "Отлично. Получилось событие привязать: " + memoryEvent.getShortName() +
                        " к событию " + parent.getShortName(),
                deleteStrategy, hp
        );
    }

}
