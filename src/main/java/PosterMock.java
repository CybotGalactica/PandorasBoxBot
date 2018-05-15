public class PosterMock implements PandoraWebsitePoster{
    @Override
    public String attemptLogin(String username, String password) {
        return null;
    }

    @Override
    public void postKillCode(Integer id, String code) {
        System.out.println(String.format("Posted kill code: %1$2s", code));
    }

    @Override
    public void postPuzzleCode(Integer id, String code) {
        System.out.println(String.format("Posted puzzle code: %1$2s", code));
    }

    @Override
    public String acquireKillCodeFromText(String text) {
        return text;
    }

    @Override
    public String acquirePuzzleCodeFromText(String text) {
        return text;
    }

    @Override
    public String getKillCodeArgumentDescription() {
        return "description";
    }

    @Override
    public String getPuzzleCodeArgumentDescription() {
        return "puzzledescription";
    }
}