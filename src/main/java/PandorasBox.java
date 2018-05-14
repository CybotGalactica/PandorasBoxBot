import com.google.auto.service.AutoService;
import org.apache.commons.io.FileUtils;
import org.simonscode.telegrambots.framework.Bot;
import org.simonscode.telegrambots.framework.Module;
import org.simonscode.telegrambots.framework.ModuleInfo;
import org.simonscode.telegrambots.framework.State;
import org.telegram.telegrambots.api.methods.GetFile;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.PhotoSize;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.exceptions.TelegramApiException;

import java.io.IOException;
import java.io.File;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

@AutoService(Module.class)
public class PandorasBox implements Module {
    private static final String NAME = "PBPB-09";
    private static final String VERSION = "0.0.1-SNAPSHOT";
    private static final String AUTHOR = "N.M. Overkamp - CFO CG";

    private OCRProvider ocrProvider;
    private PandoraWebsitePoster pandoraWebsitePoster;

    @Override
    public ModuleInfo getModuleInfo() {
        return new ModuleInfo(NAME, VERSION, AUTHOR, ModuleInfo.InstanciationPereference.SINGLE_INSTANCE_ACROSS_ALL_BOTS);
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
    public void postLoad(Bot bot) {

    }

    private String[] split(String text) {
        return text.split("\\s+");
    }

    @Override
    public void processUpdate(Bot sender, Update update) {
        if (update.hasMessage()) {
            Message message = update.getMessage();
            String caption = message.getCaption();
            if (caption != null) {
                String[] arguments = split(caption);
                if (arguments.length > 0 && (arguments[0].equals("kill") || arguments[0].equals("puzzle"))) {
                    if (message.hasPhoto()) {
                        @SuppressWarnings("ConstantConditions")
                        String fileId = message.getPhoto().stream().max((Comparator.comparingInt(PhotoSize::getFileSize))).get().getFileId();
                        try {
                            Path tempFile = Files.createTempFile(fileId, "tmp");
                            FileUtils.copyURLToFile(new URL(sender.execute(new GetFile().setFileId(fileId)).getFileUrl(sender.getBotToken())), tempFile.toFile());
                            String text = ocrProvider.read(com.google.common.io.Files.toByteArray(new File(tempFile.toAbsolutePath().toString())));
                            if (caption.contains("kill")) {
                                pandoraWebsitePoster.postKillCode(pandoraWebsitePoster.acquireKillCodeFromText(text), arguments);
                            } else if (caption.contains("puzzle")) {
                                pandoraWebsitePoster.postPuzzleCode(pandoraWebsitePoster.acquirePuzzleCodeFromText(text), arguments);
                            }
                        } catch (TelegramApiException | IOException | OCRException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }

    @Override
    public void preUnload(Bot bot) {
        saveState(bot);
    }

    @Override
    public void postUnload(Bot bot) {

    }

    @Override
    public State saveState(Bot bot) {
        return null;
    }

    @Override
    public Class<? extends State> getStateType() {
        return null;
    }
}
