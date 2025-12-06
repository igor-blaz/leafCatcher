package leafCatcher.history;

public enum ActionType {
    INTRO(2),
    START(2),
    DO_ACTION(2),
    GO_BACK(2),

    BACK_OR_FORWARD_QUESTION(2),
    WRITE_NEXT_QUESTION(2),
    I_DONT_KNOW(2),


    GET_ROOT(2),
    ROOT_IS_ABSENCE_INFO(2),
    ROOT_DESCRIPTION_CREATION(2),
    ROOT_BUTTON_CREATION(2),

    CHILD_DESCRIPTION_AWAIT(2),
    CHILD_DESCRIPTION_CREATION(2),
    CHILD_BUTTON_CREATION(2),
    GET_CHILD(2),
    REPEAT_CURRENT(2),


    PUT_IN_MEMORY(2),
    BOND(2),
    DELETE(2),

    END_IS_ABSENCE_INFO(2),
    GET_ENDING(2),
    ENDING_DESCRIPTION_CREATION(2),
    ENDING_BUTTON_CREATION(2),
    AFTER_END_CHOICE(2),

    RANDOM(2),
    CREDITS(2),
    ADMIN_MODE(2),
    ERROR(2);

    private final int lifeTime;

    ActionType(int lifeTime) {
        this.lifeTime = lifeTime;
    }
    public int getLifeTime() {
        return lifeTime;
    }
}
