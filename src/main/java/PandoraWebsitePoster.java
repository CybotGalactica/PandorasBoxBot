import java.util.Map;

public interface PandoraWebsitePoster {
    String attemptLogin(String username, String password);
    String postKillCode(Integer id, String code);
    String postPuzzleCode(Integer id, String code);
    String acquireKillCodeFromText(String text);
    String acquirePuzzleCodeFromText(String text);
    Map<String,Integer> getHumansInTeam(String sessionToken);
}
