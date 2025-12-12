package leafCatcher.history;

import leafCatcher.service.deleteStrategy.DeleteStrategy;
import lombok.Getter;

@Getter
public enum ActionType {


    INTRO(200, DeleteStrategy.DELETE_AFTER_ONE),
    START(200),
    DO_ACTION(200),
    GO_BACK(200),

    BACK_OR_FORWARD_QUESTION(200),
    WRITE_NEXT_QUESTION(200),
    I_DONT_KNOW(200, DeleteStrategy.DELETE_AFTER_ONE),


    GET_ROOT(200),
    ROOT_IS_ABSENCE_INFO(200),
    ROOT_DESCRIPTION_CREATION(200),
    ROOT_BUTTON_CREATION(200),

    CHILD_DESCRIPTION_AWAIT(200),
    CHILD_DESCRIPTION_CREATION(200),
    CHILD_BUTTON_CREATION(200),
    GET_CHILD(200),
    REPEAT_CURRENT(200),


    PUT_IN_MEMORY(200),
    BOND(200),
    DELETE(200),

    END_IS_ABSENCE_INFO(200),
    GET_ENDING(200),
    ENDING_DESCRIPTION_CREATION(200),
    ENDING_BUTTON_CREATION(200),
    AFTER_END_CHOICE(200),

    RANDOM(200),
    CREDITS(200),
    ADMIN_MODE(200),
    ERROR(200);

    private final int lifeTime;
    private final DeleteStrategy deleteStrategy;


    ActionType(int lifeTime) {
        this.lifeTime = lifeTime;
        this.deleteStrategy = DeleteStrategy.NONE;
    }

    ActionType(int lifeTime, DeleteStrategy deleteStrategy) {
        this.lifeTime = lifeTime;
        this.deleteStrategy = deleteStrategy;
    }
}
