public enum ActionType {
    PUZZLE(1), KILL(2), UNSET(0);

    private final int value;

    ActionType(int value) {
        this.value = value;
    }

    static ActionType fromInt(int value) {
        switch (value) {
            case 1:
                return PUZZLE;
            case 2:
                return KILL;
            default:
                return UNSET;
        }
    }

    int toInt() {
        return value;
    }
}
