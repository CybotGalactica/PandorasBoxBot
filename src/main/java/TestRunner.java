import org.simonscode.telegrambots.framework.*;
import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.TelegramBotsApi;
import org.telegram.telegrambots.exceptions.TelegramApiRequestException;

import java.util.Collections;

public class TestRunner {

    private static final String testApiKey = "389396124:AAEnCRteN805rxWGMkFTbKMjpJoA5nvd3p8";
    private static final String testBotUsername = "PBPB-09";
    private static final Module testModule = new PandorasBox();

    public static void main(String[] args) throws TelegramApiRequestException {
        ApiContextInitializer.init();
        TelegramBotsApi api = new TelegramBotsApi();

        Bot bot = new Bot(testBotUsername, testApiKey, Collections.singletonList(testModule));
        testModule.preLoad(bot);
        api.registerBot(bot);
        testModule.postLoad(bot);
    }
}
