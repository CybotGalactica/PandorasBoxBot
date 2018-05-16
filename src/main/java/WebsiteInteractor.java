import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.jsoup.Connection;
import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import website.KillCode;
import website.PuzzleCode;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WebsiteInteractor implements PandoraWebsitePoster {

    private static final Gson gson = new GsonBuilder().create();
    private static final Pattern memberInTeam = Pattern.compile("'<option value=\"(\\d+)\">([^<]+)</option>' \\+");

    @Override
    public String attemptLogin(String username, String password) {
        try {
            Response resp = Jsoup.connect("https://www.iapandora.nl/auth/endpoint/login")
                                            .requestBody("{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}\t")
                                            .method(Connection.Method.POST)
                                            .execute();
            StringBuilder sb = new StringBuilder();
            sb.append("Headers:\n");
            for (Map.Entry<String, String> header : resp.headers().entrySet()) {
                sb.append(header.getKey());
                sb.append(" : ");
                sb.append(header.getValue());
                sb.append('\n');
            }
            sb.append('\n');
            for (Map.Entry<String, String> cookie : resp.cookies().entrySet()) {
                sb.append(cookie.getKey());
                sb.append(" : ");
                sb.append(cookie.getValue());
                sb.append('\n');
            }
            sb.append('\n');
            sb.append(resp.body());
            Files.write(new File("./output.log").toPath(), sb.toString().getBytes());
            return "";
//            LoginResponse loginResponse = gson.fromJson(doc.wholeText(), LoginResponse.class);
//            if (loginResponse.logged_in) {
//                return "";
//            }
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
        return null;
    }

    @Override
    public String acquirePuzzleCodeFromText(String text) {
        return null;
    }

    @Override
    public Map<String, Integer> getHumansInTeam(String sessionToken) {
        try {
            Document doc = Jsoup.connect("").get();
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
