import java.util.HashMap;
import java.util.Map;

public class PosterMock implements PandoraWebsitePoster {
    @Override
    public String attemptLogin(String username, String password) {
        return "TOKEN";
    }

    @Override
    public String postKillCode(Integer id, String code) {
        System.out.println(String.format("Posted kill code: %1$2s", code));
        return "SUCCES!";
    }

    @Override
    public String postPuzzleCode(Integer id, String code) {
        System.out.println(String.format("Posted puzzle code: %1$2s", code));
        return "SUCCESS!";
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
    public Map<String, Integer> getHumansInTeam(String sessionToken) {
        Map<String, Integer> users = new HashMap<>();
        users.put("Kim Ti-Sin-Ya", 40);
        users.put("Kim Uk-Ul", 46);
        users.put("Kim Yo-Na", 52);
        users.put("Kim Bei-Tse-Kun", 54);
        users.put("Kim Meer Thee", 58);
        users.put("Kim Mas-To", 78);
        return users;
    }
}
