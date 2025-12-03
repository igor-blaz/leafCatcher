package leafCatcher.history;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


@Service
public class DraftService {
    private final Map<Long, Draft> drafts = new ConcurrentHashMap<>();
    static final String DESCRIPTION_NOT_FOUND = "☹️ Извините, но описание для этого события отсутствует." +
            " Попробуйте пересоздать";
    static final String BUTTON_NAME_NOT_FOUND = "☹️ Извините, но текст кнопки для этого события отсутствует." +
            " Попробуйте пересоздать";
    static final String END_DESCRIPTION_NOT_FOUND = "☹️ Извините, у этой концовки нет описания. Попробуйте пересоздать";

    private Draft getOrCreate(Long userId) {
        return drafts.computeIfAbsent(userId, id -> {
            var d = new Draft();
            d.setDepthCounter(0); // если Integer -> лучше сделать int
            return d;
        });
    }

    public Draft get(Long userId) {
        return drafts.get(userId);
    }

    //Концовки
    public void setDraftEndingDescription(Long userId, String text) {
        getOrCreate(userId).setEndDescription(text);
    }

    public void setEndingButtonName(Long userId, String text) {
        getOrCreate(userId).setEndButtonName(text);
    }

    public String getEndingDescription(Long userId) {
        Draft draft = drafts.get(userId);
        if (draft != null) {
            return draft.getEndDescription();
        }
        return END_DESCRIPTION_NOT_FOUND;
    }

    public String getEndingButtonName(Long userId) {
        Draft draft = drafts.get(userId);
        if (draft != null) {
            return draft.getEndButtonName();
        }
        return END_DESCRIPTION_NOT_FOUND;
    }

    //Корень
    public void setRootDescription(Long userId, String text) {
        getOrCreate(userId).setRootDescription(text);
    }

    public void setRootButtonName(Long userId, String text) {
        getOrCreate(userId).setRootButtonName(text);
    }


    //Обычные события
    public void setChildDescription(Long userId, String text) {
        getOrCreate(userId).setChildDescription(text);
    }

    public void setChildButtonName(Long userId, String text) {
        getOrCreate(userId).setChildButtonName(text);
    }

    public String getChildDescription(Long userId) {
        Draft draft = drafts.get(userId);
        if (draft != null) {
            return draft.getChildDescription();
        }
        return DESCRIPTION_NOT_FOUND;
    }
    public String getChildButtonName(Long userId) {
        Draft draft = drafts.get(userId);
        if (draft != null) {
            return draft.getChildButtonName();
        }
        return BUTTON_NAME_NOT_FOUND;
    }

    public void incDepth(Long userId) {
        getOrCreate(userId).increaseDepthCounter();
    }

    public void decDepth(Long userId) {
        getOrCreate(userId).decreaseDepthCounter();
    }

    public void resetDepth(Long userId) {
        getOrCreate(userId).makeZeroDepthCounter();
    }
}