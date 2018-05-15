public interface PandoraWebsitePoster {
    String attemptLogin(String username, String password);
    void postKillCode(Integer id, String code);
    void postPuzzleCode(Integer id, String code);
    String acquireKillCodeFromText(String text);
    String acquirePuzzleCodeFromText(String text);
    String getKillCodeArgumentDescription();
    String getPuzzleCodeArgumentDescription();
}
