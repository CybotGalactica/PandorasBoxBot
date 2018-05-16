import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.jsoup.Connection;
import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import website.KillCode;
import website.LoginResponse;
import website.PuzzleCode;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WebsiteInteractor implements PandoraWebsitePoster {

    private static final Gson gson = new GsonBuilder().create();
    private static final Pattern memberInTeam = Pattern.compile("'<option value=\"(\\d+)\">([^<]+)</option>' \\+");
    private static final Pattern[] killPatterns = {Pattern.compile("Personal\\s+code\\s*:\\s*([a-zA-Z0-9]{10})")};
    private static final Pattern[] puzzlePatterns = {Pattern.compile("From\\s*:\\s*([a-zA-Z0-9]{15})")};

    @Override
    public String attemptLogin(String username, String password) {
        try {
            Response initialPage = Jsoup.connect("https://www.iapandora.nl/auth/")
                                        .method(Connection.Method.GET)
                                        .execute();

            Response resp = Jsoup.connect("https://www.iapandora.nl/auth/endpoint/login")
                                 .requestBody("{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}\t")
                                 .method(Connection.Method.POST)
                                 .cookie("csrftoken", initialPage.cookie("csrftoken"))
                                 .header("Content-Type", "application/json")
                                 .header("DNT", "1")
                                 .header("Connection", "keep-alive")
                                 .header("Referer", "https://www.iapandora.nl/auth/")
                                 .header("Host", "www.iapandora.nl")
                                 .header("X-CSRFToken", initialPage.cookie("csrftoken"))
                                 .header("X-Requested-With", "XMLHttpRequest")
                                 .header("User-Agent", "Mozilla/5.0 (X11; Linux x86_64; rv:61.0) Gecko/20100101 Firefox/61.0")
                                 .ignoreContentType(true)
                                 .execute();
            LoginResponse loginResponse = gson.fromJson(resp.body(), LoginResponse.class);
            if (loginResponse.logged_in) {
                return resp.cookie("sessionid");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public String postKillCode(Integer id, String code) {
        try {
            KillCode killCode = new KillCode();
            killCode.kill_code = code;
            killCode.member_id = String.valueOf(Database.getKillerId(id));
            Document response = Jsoup.connect("https://www.iapandora.nl/killcode").requestBody(gson.toJson(killCode)).post();
            return response.text();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public String postPuzzleCode(Integer id, String code) {
        try {
            PuzzleCode puzzleCode = new PuzzleCode();
            puzzleCode.puzzle_code = code;
            Document response = Jsoup.connect("https://www.iapandora.nl/puzzlecode").requestBody(gson.toJson(puzzleCode)).post();
            return response.text();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;

    }

    @Override
    public String acquireKillCodeFromText(String text) {
        for (Pattern killCodePattern : killPatterns) {
            Matcher m = killCodePattern.matcher(text);
            if (m.find()) {
                return m.group(1);
            }
        }
        return null;
    }

    @Override
    public String acquirePuzzleCodeFromText(String text) {
        for (Pattern puzzleCodePattern : puzzlePatterns) {
            Matcher m = puzzleCodePattern.matcher(text);
            if (m.find()) {
                return m.group(1);
            }
        }
        return null;
    }

    @Override
    public Map<String, Integer> getHumansInTeam(String sessionToken) {
        try {
            Document doc = Jsoup.connect("https://www.iapandora.nl").get();
            String js = doc.getElementsByTag("header").first().children().last().data();

            String[] lines = js.split("\n");
            Map<String, Integer> users = new HashMap<>();
            for (String line : lines) {
                if (line.trim().isEmpty()) {
                    continue;
                }
                Matcher matcher = memberInTeam.matcher(line.trim());
                if (matcher.matches()) {
                    users.put(matcher.group(2), Integer.parseInt(matcher.group(1)));
                }
            }
            return users;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
