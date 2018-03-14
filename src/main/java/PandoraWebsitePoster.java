public interface PandoraWebsitePoster {
    void postKillCode(String code, String[] argv);
    void postPuzzleCode(String code, String[] argv);
    String acquireKillCodeFromText(String text);
    String acquirePuzzleCodeFromText(String text);
    String getKillCodeArgumentDescription();
    String getPuzzleCodeArgumentDescription();
}
