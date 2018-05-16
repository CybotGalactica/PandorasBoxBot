import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.jsoup.Connection;
import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import website.LoginResponse;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WebsiteInteractor implements PandoraWebsitePoster {

    private static final Gson gson = new GsonBuilder().create();
    private static final Pattern memberInTeam = Pattern.compile("'<option value=\"(\\d+)\">([^<]+)</option>' \\+");
    private static final Pattern[] killPatterns = {Pattern.compile("[a-zA-Z0-9]{10}")};
    private static final Pattern[] puzzlePatterns = {Pattern.compile(".*From\\s*:\\s*([a-zA-Z0-9]{15}).*")};

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
            String csrf = getCSRFToken();

            Document doc = Jsoup.connect("https://www.iapandora.nl/killcode")
                                .cookie("csrftoken", csrf)
                                .cookie("sessionid", Database.getSessionToken(id))
                                .header("Content-Type", "application/json")
                                .header("Referer", "https://www.iapandora.nl/")
                                .header("Host", "www.iapandora.nl")
                                .header("X-CSRFToken", csrf)
                                .header("X-Requested-With", "XMLHttpRequest")
                                .header("User-Agent", "Mozilla/5.0 (X11; Linux x86_64; rv:61.0) Gecko/20100101 Firefox/61.0")
                                .requestBody("{\"kill_code\":\"" + code + "\",\"member_id\":\"" + Database.getKillerId(id) + "\"}")
                                .ignoreHttpErrors(true)
                                .post();
            return doc.text();
        } catch (IOException e) {
            e.printStackTrace();
            return e.getMessage();
        }
    }

    @Override
    public String postPuzzleCode(Integer id, String code) {
        System.out.println(code);
        try {
            String csrf = getCSRFToken();

            Document doc = Jsoup.connect("https://www.iapandora.nl/puzzlecode")
                                .cookie("csrftoken", csrf)
                                .cookie("sessionid", Database.getSessionToken(id))
                                .header("Content-Type", "application/json")
                                .header("Referer", "https://www.iapandora.nl/")
                                .header("Host", "www.iapandora.nl")
                                .header("X-CSRFToken", csrf)
                                .header("X-Requested-With", "XMLHttpRequest")
                                .header("User-Agent", "Mozilla/5.0 (X11; Linux x86_64; rv:61.0) Gecko/20100101 Firefox/61.0")
                                .requestBody("{\"puzzle_code\":\"" + code + "\"}")
                                .ignoreHttpErrors(true)
                                .post();
            return doc.text();
        } catch (IOException e) {
            e.printStackTrace();
            return e.getMessage();
        }
    }

    @SuppressWarnings("Duplicates")
    @Override
    public String acquireKillCodeFromText(String text) {
        String replaced = text.replaceAll("\\s+", " ");
        for (Pattern killCodePattern : killPatterns) {
            Matcher m = killCodePattern.matcher(replaced);
            if (m.find()) {
                return m.group(1);
            }
        }
        String[] parts = replaced.split("\\s+");
        for (String part : parts) {
            if (part.length() == 10) {
                return part;
            }
        }
        return null;
    }

    @SuppressWarnings("Duplicates")
    @Override
    public String acquirePuzzleCodeFromText(String text) {
        String replaced = text.replaceAll("\\s+", " ");
        for (Pattern puzzleCodePattern : puzzlePatterns) {
            Matcher m = puzzleCodePattern.matcher(replaced);
            if (m.find()) {
                return m.group(1);
            }
        }
        String[] parts = replaced.split("\\s+");
        for (String part : parts) {
            if (part.length() == 10) {
                return part;
            }
        }
        return null;
    }

    @Override
    public Map<String, Integer> getHumansInTeam(String sessionToken) {
        try {
            String csrf = getCSRFToken();

            Document doc = Jsoup.connect("https://www.iapandora.nl")
                                .cookie("csrftoken", csrf)
                                .cookie("sessionid", sessionToken)
                                .header("Connection", "keep-alive")
                                .header("Referer", "https://www.iapandora.nl/")
                                .header("Host", "www.iapandora.nl")
                                .header("X-CSRFToken", csrf)
                                .header("X-Requested-With", "XMLHttpRequest")
                                .header("User-Agent", "Mozilla/5.0 (X11; Linux x86_64; rv:61.0) Gecko/20100101 Firefox/61.0")
                                .get();

            String js = doc.getElementsByTag("header").first().children().last().data();

            String[] lines = js.split("\n");
            Map<String, Integer> users = new HashMap<>();
            for (String line : lines) {
                if (line.trim().isEmpty()) {
                    continue;
                }
                Matcher matcher = memberInTeam.matcher(line.trim());
                if (matcher.find()) {
                    users.put(matcher.group(2), Integer.parseInt(matcher.group(1)));
                }
            }
            return users;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static String getCSRFToken() throws IOException {
        return Jsoup.connect("https://www.iapandora.nl/auth/")
                    .method(Connection.Method.GET)
                    .execute().cookie("csrftoken");
    }
}
