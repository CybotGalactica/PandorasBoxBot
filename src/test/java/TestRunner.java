import org.simonscode.telegrambots.framework.*;
import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.TelegramBotsApi;
import org.telegram.telegrambots.exceptions.TelegramApiRequestException;

import java.util.Collections;

public class TestRunner {

    private static final String testBotUsername = "PBPB-09";
    private static final Module testModule = new PandorasBox();

    public static void main(String[] args) throws TelegramApiRequestException {
        ApiContextInitializer.init();
        TelegramBotsApi api = new TelegramBotsApi();

        Bot bot = new Bot(testBotUsername, args[0], Collections.singletonList(testModule));
        testModule.preLoad(bot);
        api.registerBot(bot);
        testModule.postLoad(bot);
    }
}