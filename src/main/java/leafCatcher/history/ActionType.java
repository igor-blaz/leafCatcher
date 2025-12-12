package leafCatcher.history;

import leafCatcher.service.deleteStrategy.DeleteStrategy;
import lombok.Getter;

@Getter
public enum ActionType {


    INTRO(200, DeleteStrategy.DELETE_AFTER_ONE),
    START(200, DeleteStrategy.DELETE_AFTER_N_MESSAGES),
    DO_ACTION(200, DeleteStrategy.DELETE_ON_NEXT),
    GO_BACK(200, DeleteStrategy.DELETE_ON_NEXT),

    BACK_OR_FORWARD_QUESTION(200, DeleteStrategy.DELETE_ON_NEXT),
    WRITE_NEXT_QUESTION(200, DeleteStrategy.DELETE_ON_NEXT),
    I_DONT_KNOW(200, DeleteStrategy.DELETE_AFTER_ONE),


    GET_ROOT(200, DeleteStrategy.DELETE_ON_NEXT),
    ROOT_IS_ABSENCE_INFO(200, DeleteStrategy.DELETE_ON_NEXT),
    ROOT_DESCRIPTION_CREATION(200, DeleteStrategy.DELETE_ON_NEXT),
    ROOT_BUTTON_CREATION(200, DeleteStrategy.DELETE_ON_NEXT),

    CHILD_DESCRIPTION_AWAIT(200, DeleteStrategy.DELETE_ON_NEXT),
    CHILD_DESCRIPTION_CREATION(200, DeleteStrategy.DELETE_ON_NEXT),
    CHILD_BUTTON_CREATION(200, DeleteStrategy.DELETE_ON_NEXT),
    GET_CHILD(200, DeleteStrategy.DELETE_ON_NEXT),
    REPEAT_CURRENT(200, DeleteStrategy.DELETE_ON_NEXT),


    PUT_IN_MEMORY(200, DeleteStrategy.DELETE_ON_NEXT),
    BOND(200, DeleteStrategy.DELETE_ON_NEXT),
    DELETE(200, DeleteStrategy.DELETE_ON_NEXT),

    END_IS_ABSENCE_INFO(200, DeleteStrategy.DELETE_ON_NEXT),
    GET_ENDING(200, DeleteStrategy.DELETE_ON_NEXT),
    ENDING_DESCRIPTION_CREATION(200, DeleteStrategy.DELETE_ON_NEXT),
    ENDING_BUTTON_CREATION(200, DeleteStrategy.DELETE_ON_NEXT),
    AFTER_END_CHOICE(200, DeleteStrategy.DELETE_ON_NEXT),

    RANDOM(200, DeleteStrategy.DELETE_ON_NEXT),
    CREDITS(200, DeleteStrategy.DELETE_ON_NEXT),
    ADMIN_MODE(200, DeleteStrategy.DELETE_ON_NEXT),
    ERROR(200, DeleteStrategy.DELETE_ON_NEXT);

    private final int lifeTime;
    private final DeleteStrategy deleteStrategy;


    ActionType(int lifeTime, DeleteStrategy deleteStrategy) {
        this.lifeTime = lifeTime;
        this.deleteStrategy = deleteStrategy;
    }
}
