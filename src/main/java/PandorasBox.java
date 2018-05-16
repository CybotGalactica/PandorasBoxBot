import com.google.auto.service.AutoService;
import org.apache.commons.io.FileUtils;
import org.simonscode.telegrambots.framework.Bot;
import org.simonscode.telegrambots.framework.Module;
import org.simonscode.telegrambots.framework.ModuleAdapter;
import org.simonscode.telegrambots.framework.ModuleInfo;
import org.telegram.telegrambots.api.methods.GetFile;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.CallbackQuery;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.PhotoSize;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.exceptions.TelegramApiException;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.*;

@AutoService(Module.class)
public class PandorasBox extends ModuleAdapter {
    private static final String NAME = "PBPB-09";
    private static final String VERSION = "0.0.1-SNAPSHOT";
    private static final String AUTHOR = "N.M. Overkamp - CFO CG";
    private final String QUERY_KILL = "Killcode";
    private final String QUERY_PUZZLE = "Puzzlecode";
    private final String QUERY_LOGOUT = "Logout and Forget me";

    private OCRProvider ocrProvider;
    private PandoraWebsitePoster pandoraWebsitePoster;
    private Bot bot;

    @SuppressWarnings("WeakerAccess")
    public PandorasBox() {
        pandoraWebsitePoster = new WebsiteInteractor();
        try {
            ocrProvider = new GCVision();
        } catch (OCRException e) {
            e.printStackTrace();
        }
    }

    @Override
    public ModuleInfo getModuleInfo() {
        return new ModuleInfo(NAME, VERSION, AUTHOR, ModuleInfo.InstanciationPereference.SINGLE_INSTANCE_ACROSS_ALL_BOTS);
    }

    @Override
    public void processUpdate(Bot sender, Update update) {
        if (update.hasMessage()) {
            Message message = update.getMessage();
            Integer id = message.getFrom().getId();
            if (message.hasText()) {
                int state = Database.getLoginState(id);
                switch (state) {
                    case 0: // Never seen before
                        sendInitialMessage(id);
                        Database.setLoginState(id, 1);
                        break;
                    case 1: // Entered Username
                        Database.setUsername(id, message.getText());
                        sendPasswordMessage(id);
                        Database.setLoginState(id, 2);
                        break;
                    case 2: //Entered Password
                        String username = Database.getUsername(id);
                        if (username == null || username.isEmpty() || username.length() > 50) {
                            Database.setLoginState(id, 1);
                            sendUsernameErrorMessage(id);
                            break;
                        }
                        String sessionToken = pandoraWebsitePoster.attemptLogin(username, message.getText());
                        if (sessionToken == null) {
                            sendLoginErrorMessage(id);
                            Database.setLoginState(id, 1);
                            break;
                        }
                        Database.setUsername(id, null);
                        Database.setSessionToken(id, sessionToken);
                        sendLoginSuccessAndSelectKillerMessage(id, sessionToken);
                        Database.setLoginState(id, 3);
                        break;
                    case 4: // Login successful
                        switch (message.getText()) {
                            case QUERY_KILL:
                                Database.setActionType(id, ActionType.KILL);
                                break;
                            case QUERY_PUZZLE:
                                Database.setActionType(id, ActionType.PUZZLE);
                                break;
                            case QUERY_LOGOUT:
                                Database.forget(id);
                                sendForgetSuccessMessage(id);
                                break;
                            default:
                                ActionType type = Database.getActionType(id);
                                switch (type) {
                                    case KILL:
                                        String killResponse = pandoraWebsitePoster.postKillCode(id, message.getText());
                                        sendCodeSubmitResponse(id, killResponse);
                                        break;
                                    case PUZZLE:
                                        String puzzleResponse = pandoraWebsitePoster.postPuzzleCode(id, message.getText());
                                        sendCodeSubmitResponse(id, puzzleResponse);
                                        break;
                                    case UNSET:
                                    default:
                                        sendActionFirstError(id);
                                        break;
                                }
                        }
                        break;
                    default:
                        Database.setLoginState(id, 1);
                        sendInitialMessage(id);
                        break;
                }
            }

            if (message.hasPhoto()) {
                switch (Database.getLoginState(id)) {
                    case 3:
                    case 4:
                        @SuppressWarnings("ConstantConditions")
                        String fileId = message.getPhoto().stream().max((Comparator.comparingInt(PhotoSize::getFileSize))).get().getFileId();
                        try {
                            Path tempFile = Files.createTempFile(fileId, "tmp");
                            FileUtils.copyURLToFile(new URL(sender.execute(new GetFile().setFileId(fileId)).getFileUrl(sender.getBotToken())), tempFile.toFile());
                            String text = ocrProvider.read(com.google.common.io.Files.toByteArray(new File(tempFile.toAbsolutePath().toString())));
                            switch (Database.getActionType(id)) {
                                case KILL:
                                    String killCode = pandoraWebsitePoster.acquireKillCodeFromText(text);
                                    if (killCode == null) {
                                        System.out.println("No killCode in:" + text.replaceAll("\\s+", " "));
                                        sendCantFindCodeMessage(id);
                                        break;
                                    }
                                    String killResponse = pandoraWebsitePoster.postKillCode(id, killCode);
                                    sendCodeSubmitResponse(id, killResponse);
                                    break;
                                case PUZZLE:
                                    String puzzleCode = pandoraWebsitePoster.acquirePuzzleCodeFromText(text);
                                    if (puzzleCode == null) {
                                        System.out.println("No puzzlecode in:" + text.replaceAll("\\s+", " "));
                                        sendCantFindCodeMessage(id);
                                        break;
                                    }
                                    String puzzleResponse = pandoraWebsitePoster.postPuzzleCode(id, puzzleCode);
                                    sendCodeSubmitResponse(id, puzzleResponse);
                                    break;
                                default:
                                    sendActionFirstError(id);
                                    break;
                            }
                        } catch (TelegramApiException | IOException | OCRException e) {
                            e.printStackTrace();
                        }
                        break;
                    default:
                        sendLoginFirstMessage(id);
                        break;
                }
            }
        } else if (update.hasCallbackQuery()) {
            Integer id = update.getCallbackQuery().getFrom().getId();
            CallbackQuery query = update.getCallbackQuery();
            switch (query.getData()) {
                case QUERY_LOGOUT:
                    Database.forget(id);
                    sendForgetSuccessMessage(id);
                    break;
                default:
                    try {
                        int killerId = Integer.parseInt(query.getData());
                        Database.setKillerId(id, killerId);
                        sendReadyMessage(id);
                        Database.setLoginState(id, 4);
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                        System.err.println("WFT?!");
                    }
                    break;
            }
        }
    }

    private void sendInitialMessage(Integer id) {
        InlineKeyboardMarkup replyMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> buttons = new ArrayList<>();
        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setText("Forget me");
        button.setCallbackData(QUERY_LOGOUT);
        buttons.add(Collections.singletonList(button));
        replyMarkup.setKeyboard(buttons);
        sendMessage(id, "Welcome to the PandoraCodeBot!\n" +
                                "To use it, you will have to login to the Pandora website.\n" +
                                "We promise you that we do not store your password use it to optain a sesstion token.\n" +
                                "We can use that to submit codes without knowing your password.\n" +
                                "Make us forget all data about you by clicking the button below.\n" +
                                "\n" +
                                "Enjoy!\n" +
                                "Simon Struck and Niels Overkamp", replyMarkup);
        try {
            Thread.sleep(1_000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        sendMessage(id, "Username:");
    }

    private void sendPasswordMessage(Integer id) {
        sendMessage(id, "Password:");
    }

    private void sendUsernameErrorMessage(Integer id) {
        sendMessage(id, "Something must have gone wrong...\n\nCould you start again by typing in your username:");
    }

    private void sendLoginErrorMessage(Integer id) {
        sendMessage(id, "Nope, that did not work. Try again, please.\n\nPlease type your username:");
    }

    private void sendLoginSuccessAndSelectKillerMessage(Integer id, String sessionToken) {
        Map<String, Integer> players = pandoraWebsitePoster.getHumansInTeam(sessionToken);

        InlineKeyboardMarkup replyMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> buttons = new ArrayList<>();
        for (Map.Entry<String, Integer> name : players.entrySet()) {
            InlineKeyboardButton button = new InlineKeyboardButton();
            button.setText(name.getKey());
            button.setCallbackData(String.valueOf(name.getValue()));
            buttons.add(Collections.singletonList(button));
        }
        replyMarkup.setKeyboard(buttons);
        sendMessage(id, "Congrats! We have logged you in, remembered the sessionToken.\nPlease select Who you are now:", replyMarkup);
    }

    private void sendForgetSuccessMessage(Integer id) {
        sendMessage(id, "I've successfully forgotten all about your existance. Send /start to begin using the bot again.", new ReplyKeyboardRemove());
    }

    private void sendCodeSubmitResponse(Integer id, String response) {
        if (response == null) {
            sendMessage(id, "Failed!\n");
        } else {
            sendMessage(id, response);
        }
    }

    private void sendActionFirstError(Integer id) {
        ReplyKeyboardMarkup keyboard = new ReplyKeyboardMarkup();
        keyboard.setOneTimeKeyboard(false);
        KeyboardRow firstRow = new KeyboardRow();
        firstRow.add(QUERY_PUZZLE);
        firstRow.add(QUERY_KILL);
        KeyboardRow secondRow = new KeyboardRow();
        secondRow.add(QUERY_LOGOUT);
        keyboard.setKeyboard(Arrays.asList(firstRow, secondRow));
        sendMessage(id, "Please tell me first what type of code this is!", keyboard);
    }

    private void sendCantFindCodeMessage(Integer id) {
        sendMessage(id, "Sorry, I could not find the code in the image.\nMaybe try again or type it by hand?");
    }

    private void sendLoginFirstMessage(Integer id) {
        sendMessage(id, "Please login first.\n" +
                                "\n" +
                                "Username:");
    }

    private void sendReadyMessage(Integer id) {
        ReplyKeyboardMarkup keyboard = new ReplyKeyboardMarkup();
        keyboard.setOneTimeKeyboard(false);
        KeyboardRow firstRow = new KeyboardRow();
        firstRow.add(QUERY_PUZZLE);
        firstRow.add(QUERY_KILL);
        KeyboardRow secondRow = new KeyboardRow();
        secondRow.add(QUERY_LOGOUT);
        keyboard.setKeyboard(Arrays.asList(firstRow, secondRow));
        sendMessage(id, "Great, you are now all set up!\n" +
                                "\n" +
                                "Please select the type of code that you want to submit and then either type the code or send us a photo of it.", keyboard);
    }

    private void sendMessage(Integer id, String text, ReplyKeyboard keyboard) {
        SendMessage sm = new SendMessage();
        sm.setText(text);
        sm.setChatId(String.valueOf(id));
        sm.setReplyMarkup(keyboard);
        try {
            bot.execute(sm);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void sendMessage(Integer userId, String text) {
        SendMessage sm = new SendMessage();
        sm.setText(text);
        sm.setChatId(String.valueOf(userId));
        try {
            bot.execute(sm);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void preLoad(Bot bot) {
        this.bot = bot;
    }

    @Override
    public void preUnload(Bot bot) {
        try {
            Database.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
