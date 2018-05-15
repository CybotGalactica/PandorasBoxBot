package org.nielsoverkamp.pandorabox.pbpb09.K9VS;

public interface PandoraWebsitePoster {
    void postKillCode(String code, String[] argv);
    void postPuzzleCode(String code, String[] argv);
    String acquireKillCodeFromText(String text);
    String acquirePuzzleCodeFromText(String text);
}
