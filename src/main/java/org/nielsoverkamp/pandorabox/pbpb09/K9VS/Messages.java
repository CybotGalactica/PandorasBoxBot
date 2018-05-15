package org.nielsoverkamp.pandorabox.pbpb09.K9VS;

public class Messages {
    private static final String CAPTION_EMPTY_OR_MALFORMED =
            "I am terrible sorry, but could you please give me the following information by replying to the original message";
    private static final String KILL_CODE_ARGUMENTS =
            "kill <killer> [message]";
    private static final String PUZZLE_CODE_ARGUMENTS =
            "puzzle [message]";

    public static String captionEmpty() {
        return String.format("%1$s\n%2$s\nor\n%3$s",
                CAPTION_EMPTY_OR_MALFORMED, KILL_CODE_ARGUMENTS, PUZZLE_CODE_ARGUMENTS);
    }

    public static String argumentsMalformed(String[] arguments) {
        if (arguments.length == 0) {
            return captionEmpty();
        } else if (arguments[0].equals("kill")) {
            return String.format("%1$s\n%2$s", CAPTION_EMPTY_OR_MALFORMED, KILL_CODE_ARGUMENTS);
        } else if (arguments[0].equals("puzzle")) {
            return String.format("%1$s\n%2$s", CAPTION_EMPTY_OR_MALFORMED, PUZZLE_CODE_ARGUMENTS);
        } else {
            return captionEmpty();
        }
    }
}
