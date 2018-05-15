package org.nielsoverkamp.pandorabox.pbpb09.K9VS;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PosterMock implements PandoraWebsitePoster{
    private static final Pattern[] killCodePatterns = {
            Pattern.compile("Employee code:\\s*?([0-9a-zA-Z]{8})"),
            Pattern.compile("([0-9a-zA-Z]{8})")
    };
    private static final Pattern[] puzzleCodePattern = {
            Pattern.compile("Puzzle code\n([0-9a-zA-Z]{10})"),
            Pattern.compile("([0-9a-zA-Z]{10})")
    };

    @Override
    public void postKillCode(String code, String[] argv) {
        System.out.println(String.format("Posted kill code: %1$2s", code));
    }

    @Override
    public void postPuzzleCode(String code, String[] argv) {
        System.out.println(String.format("Posted puzzle code: %1$2s", code));
    }

    @Override
    public String acquireKillCodeFromText(String text) {
        for (Pattern killCodePattern : killCodePatterns) {
            Matcher m = killCodePattern.matcher(text);
            if (m.find()) {
                return m.group(1);
            }
        }
        return null;
    }

    @Override
    public String acquirePuzzleCodeFromText(String text) {
        for (Pattern puzzleCodePattern : puzzleCodePattern) {
            Matcher m = puzzleCodePattern.matcher(text);
            if (m.find()) {
                return m.group(1);
            }
        }
        return null;
    }
}
