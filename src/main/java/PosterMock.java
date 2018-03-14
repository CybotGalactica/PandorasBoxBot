public class PosterMock implements PandoraWebsitePoster{
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
