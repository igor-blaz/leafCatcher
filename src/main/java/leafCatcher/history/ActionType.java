package leafCatcher.history;

import leafCatcher.service.deleteStrategy.DeleteStrategy;
import lombok.Getter;

@Getter
public enum ActionType {

    INTRO(2, DeleteStrategy.DELETE_BY_HP),
    START(2, DeleteStrategy.DELETE_ON_NEXT),
    DO_ACTION(2, DeleteStrategy.DELETE_ON_NEXT),
    GO_BACK(2, DeleteStrategy.DELETE_ON_NEXT),

    BACK_OR_FORWARD_QUESTION(2, DeleteStrategy.DELETE_ON_NEXT),
    WRITE_NEXT_QUESTION(2, DeleteStrategy.DELETE_ON_NEXT),
    I_DONT_KNOW(2, DeleteStrategy.DELETE_BY_HP),


    GET_ROOT(2, DeleteStrategy.DELETE_ON_NEXT),
    ROOT_IS_ABSENCE_INFO(2, DeleteStrategy.DELETE_ON_NEXT),
    ROOT_DESCRIPTION_CREATION(2, DeleteStrategy.DELETE_BY_HP),
    ROOT_BUTTON_CREATION(2, DeleteStrategy.DELETE_ON_NEXT),

    CHILD_DESCRIPTION_AWAIT(2, DeleteStrategy.DELETE_ON_NEXT),
    CHILD_DESCRIPTION_CREATION(2, DeleteStrategy.DELETE_BY_HP),
    CHILD_BUTTON_CREATION(2, DeleteStrategy.DELETE_ON_NEXT),
    GET_CHILD(2, DeleteStrategy.DELETE_ON_NEXT),
    REPEAT_CURRENT(2, DeleteStrategy.DELETE_ON_NEXT),


    PUT_IN_MEMORY(2, DeleteStrategy.DELETE_BY_HP),
    BOND(2, DeleteStrategy.DELETE_BY_HP),
    DELETE(3, DeleteStrategy.DELETE_BY_HP),

    END_IS_ABSENCE_INFO(2, DeleteStrategy.DELETE_ON_NEXT),
    GET_ENDING(2, DeleteStrategy.DELETE_BY_HP),
    ENDING_DESCRIPTION_CREATION(2, DeleteStrategy.DELETE_BY_HP),
    ENDING_BUTTON_CREATION(2, DeleteStrategy.DELETE_ON_NEXT),
    AFTER_END_CHOICE(2, DeleteStrategy.DELETE_ON_NEXT),

    RANDOM(2, DeleteStrategy.DELETE_ON_NEXT),
    CREDITS(2, DeleteStrategy.DELETE_BY_HP),
    ADMIN_MODE(2, DeleteStrategy.DELETE_ON_NEXT),
    ERROR(2, DeleteStrategy.DELETE_ON_NEXT);

    private final int lifeTime;
    private final DeleteStrategy deleteStrategy;


    ActionType(int lifeTime, DeleteStrategy deleteStrategy) {
        this.lifeTime = lifeTime;
        this.deleteStrategy = deleteStrategy;
    }
}
