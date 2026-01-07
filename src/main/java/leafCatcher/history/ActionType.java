package leafCatcher.history;

import leafCatcher.service.deleteStrategy.DeleteStrategy;
import lombok.Getter;

@Getter
public enum ActionType {

    INTRO(2, DeleteStrategy.DELETE_BY_HP),
    START(2, DeleteStrategy.DELETE_BY_HP),
    DO_ACTION(2, DeleteStrategy.DELETE_BY_HP),
    GO_BACK(2, DeleteStrategy.DELETE_BY_HP),

    BACK_OR_FORWARD_QUESTION(2, DeleteStrategy.DELETE_BY_HP),
    WRITE_NEXT_QUESTION(2, DeleteStrategy.DELETE_BY_HP),
    I_DONT_KNOW(2, DeleteStrategy.DELETE_BY_HP),


    GET_ROOT(2, DeleteStrategy.DELETE_BY_HP),
    ROOT_IS_ABSENCE_INFO(2, DeleteStrategy.DELETE_BY_HP),
    ROOT_DESCRIPTION_CREATION(2, DeleteStrategy.DELETE_BY_HP),
    ROOT_BUTTON_CREATION(2, DeleteStrategy.DELETE_BY_HP),

    CHILD_DESCRIPTION_AWAIT(2, DeleteStrategy.DELETE_BY_HP),
    CHILD_DESCRIPTION_CREATION(2, DeleteStrategy.DELETE_BY_HP),
    CHILD_BUTTON_CREATION(2, DeleteStrategy.DELETE_BY_HP),
    GET_CHILD(2, DeleteStrategy.DELETE_BY_HP),
    REPEAT_CURRENT(2, DeleteStrategy.DELETE_BY_HP),


    PUT_IN_MEMORY(2, DeleteStrategy.DELETE_BY_HP),
    BOND(2, DeleteStrategy.DELETE_BY_HP),
    BOND_BUTTON_CREATION(2, DeleteStrategy.DELETE_BY_HP),
    BOND_BUTTON_IS_ABSENCE_INFO(2, DeleteStrategy.DELETE_BY_HP),
    DELETE(3, DeleteStrategy.DELETE_BY_HP),

    END_IS_ABSENCE_INFO(2, DeleteStrategy.DELETE_BY_HP),
    GET_ENDING(2, DeleteStrategy.DELETE_BY_HP),
    ENDING_DESCRIPTION_CREATION(2, DeleteStrategy.DELETE_BY_HP),
    ENDING_BUTTON_CREATION(2, DeleteStrategy.DELETE_BY_HP),
    AFTER_END_CHOICE(2, DeleteStrategy.DELETE_BY_HP),

    RANDOM(2, DeleteStrategy.DELETE_BY_HP),
    CREDITS(2, DeleteStrategy.DELETE_BY_HP),
    ADMIN_MODE(2, DeleteStrategy.DELETE_BY_HP),
    ERROR(2, DeleteStrategy.DELETE_BY_HP);

    private final int lifeTime;
    private final DeleteStrategy deleteStrategy;


    ActionType(int lifeTime, DeleteStrategy deleteStrategy) {
        this.lifeTime = lifeTime;
        this.deleteStrategy = deleteStrategy;
    }
}
