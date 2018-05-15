import com.google.auto.service.AutoService;
import org.apache.commons.io.FileUtils;
import org.simonscode.telegrambots.framework.*;
import org.telegram.telegrambots.api.methods.GetFile;
import org.telegram.telegrambots.api.objects.CallbackQuery;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.PhotoSize;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.exceptions.TelegramApiException;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.Comparator;

@AutoService(Module.class)
public class PandorasBox extends ModuleAdapter {
    private static final String NAME = "PBPB-09";
    private static final String VERSION = "0.0.1-SNAPSHOT";
    private static final String AUTHOR = "N.M. Overkamp - CFO CG";

    private OCRProvider ocrProvider;
    private PandoraWebsitePoster pandoraWebsitePoster;

    @SuppressWarnings("WeakerAccess")
    public PandorasBox(){
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
                        sendLoginMessage();
                        Database.setLoginState(id, 1);
                        break;
                    case 1: // Entered Username
                        Database.setUsername(id, message.getText());
                        sendPasswordMessage();
                        Database.setLoginState(id, 2);
                        break;
                    case 2: //Entered Password
                        String username = Database.getUsername(id);
                        if (username == null || username.isEmpty()) {
                            Database.setLoginState(id, 1);
                            sendUsernameErrorMessage();
                            break;
                        }
                        String sessionToken = pandoraWebsitePoster.attemptLogin(username, message.getText());
                        if (sessionToken == null) {
                            sendLoginErrorMessage();
                            Database.setLoginState(id, 1);
                            break;
                        }
                        Database.setSessionToken(id, sessionToken);
                        sendReadyMessage();
                        Database.setLoginState(id, 3);
                        break;
                    case 3: // Login successful
                        ActionType type = Database.getType(id);
                        switch (type) {
                            case PUZZLE:
                                pandoraWebsitePoster.postPuzzleCode(id, message.getText());
                                break;
                            case KILL:
                                pandoraWebsitePoster.postKillCode(id, message.getText());
                                break;
                            case UNSET:
                                sendActionFirstError();
                                break;
                            default:
                                System.err.println("WFT?!");
                                break;
                        }
                        sendAlreadyLoggedInMessage();
                    default:
                        Database.setLoginState(id, 1);
                        sendLoginMessage();
                        break;
                }
            }

            if (message.hasPhoto() && Database.getLoginState(id) == 3) {
                @SuppressWarnings("ConstantConditions")
                String fileId = message.getPhoto().stream().max((Comparator.comparingInt(PhotoSize::getFileSize))).get().getFileId();
                try {
                    Path tempFile = Files.createTempFile(fileId, "tmp");
                    FileUtils.copyURLToFile(new URL(sender.execute(new GetFile().setFileId(fileId)).getFileUrl(sender.getBotToken())), tempFile.toFile());
                    String text = ocrProvider.read(com.google.common.io.Files.toByteArray(new File(tempFile.toAbsolutePath().toString())));
                    switch (Database.getType(id)) {
                        case KILL:
                            pandoraWebsitePoster.postKillCode(id, pandoraWebsitePoster.acquireKillCodeFromText(text));
                            break;
                        case PUZZLE:
                            pandoraWebsitePoster.postPuzzleCode(id, pandoraWebsitePoster.acquirePuzzleCodeFromText(text));
                            break;
                        default:
                            sendActionFirstError();
                            break;
                    }
                } catch (TelegramApiException | IOException | OCRException e) {
                    e.printStackTrace();
                }
            }
        } else if (update.hasCallbackQuery()) {
            Integer id = update.getCallbackQuery().getFrom().getId();
            CallbackQuery query = update.getCallbackQuery();
            switch (query.getData()) {
                case "kill":
                    Database.setType(id, ActionType.KILL);
                    break;
                case "puzzle":
                    Database.setType(id, ActionType.PUZZLE);
                    break;
                case "logout":
                    Database.forget(id);
                    sendForgetSuccessMessage();
                    break;
                default:
                    System.err.println("WFT?!");
                    break;
            }
        }
    }

    private void sendLoginMessage() {
    }

    private void sendPasswordMessage() {

    }

    private void sendUsernameErrorMessage() {

    }

    private void sendLoginErrorMessage() {

    }

    private void sendReadyMessage() {
        // Send permanent keyboard
    }

    private void sendAlreadyLoggedInMessage() {

    }

    private void sendActionFirstError() {

    }

    private void sendForgetSuccessMessage() {
        // Remove Keyboard
    }

    private String[] split(String text) {
        return text.split("\\s+");
    }

    @Override
    public void initialize(State state) {
        try {
            ocrProvider = new GCVision();
        } catch (OCRException e) {
            e.printStackTrace();
        }
        pandoraWebsitePoster = new PosterMock();
    }

    @Override
    public void preLoad(Bot bot) {
        //TODO read data from files
        //TODO initialize ocrProvider
        //TODO initialize pandoraWebsitePoster

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
