package org.nielsoverkamp.pandorabox;


import org.nielsoverkamp.pandorabox.pbpb09.K9VS.PandoraCodeSubmitter;
import org.nielsoverkamp.pandorabox.pbpb09.N18F.PandoraMapUtils;
import org.simonscode.telegrambots.framework.Bot;
import org.simonscode.telegrambots.framework.Module;
import org.simonscode.telegrambots.framework.State;
import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.TelegramBotsApi;
import org.telegram.telegrambots.exceptions.TelegramApiRequestException;

import java.util.Arrays;

public class TestRunner {

    private static final String testApiKey = "572150819:AAE0zDIztPQSZSNa7iniNAAvroKdp99P6Ok";
    private static final String testBotUsername = "PBPB-09";
    private static final Module[] testModules = {
            new PandoraCodeSubmitter(),
//            new PandoraMapUtils(),
    };

    public static void main(String[] args) throws TelegramApiRequestException {
        ApiContextInitializer.init();
        TelegramBotsApi api = new TelegramBotsApi();

        Bot bot = new Bot(testBotUsername, testApiKey, Arrays.asList(testModules));
        api.registerBot(bot);
        Arrays.stream(testModules).forEach((testModule) -> {
            testModule.initialize(new State());
            testModule.preLoad(bot);
            testModule.postLoad(bot);
        });
    }
}
