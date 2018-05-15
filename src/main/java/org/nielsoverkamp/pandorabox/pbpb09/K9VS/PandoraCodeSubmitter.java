package org.nielsoverkamp.pandorabox.pbpb09.K9VS;

import com.google.auto.service.AutoService;

import org.apache.commons.io.FileUtils;
import org.nielsoverkamp.pandorabox.pbpb09.Util;
import org.simonscode.telegrambots.framework.*;
import org.telegram.telegrambots.api.methods.GetFile;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.PhotoSize;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.exceptions.TelegramApiException;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;


@AutoService(Module.class)
public class PandoraCodeSubmitter extends ModuleAdapter {
    private static final String NAME = "org/nielsoverkamp/pandorabox/pbpb09/K9VS";
    private static final String VERSION = "0.0.2-SNAPSHOT";
    private static final String AUTHOR = "N.M. Overkamp - CD CG";

    private static final boolean ENABLE_CODE_POSTING = true;

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
    public void processUpdate(Bot sender, Update update) {
        Message message;
        String[] arguments;
        if (update.hasCallbackQuery()) {
            // TODO add callbackQuery support
            return;
        } else {
            if (update.hasMessage()) {
                message = update.getMessage();
            } else if (update.hasEditedMessage()) {
                message = update.getEditedMessage();
            } else if (update.hasChannelPost()) {
                message = update.getChannelPost();
            } else if (update.hasEditedChannelPost()) {
                message = update.getEditedChannelPost();
            } else {
                return;
            }
            if (!message.getFrom().getId().equals(275942348)) {
                return;
            }
            String caption = message.getCaption();
            if (caption != null) {
                arguments = Util.split(caption);
            } else if (message.hasText() ) {
                arguments = Util.split(message.getText());
            } else {
                Utils.sendFailableMessage(sender, message.getChat(), Messages.captionEmpty());
                return;
            }
        }

        if (arguments.length > 0 && (arguments[0].toLowerCase().equals("kill") || arguments[0].toLowerCase().equals("puzzle")) && ENABLE_CODE_POSTING) {
            String text;
            if (message.hasPhoto()) {
                @SuppressWarnings("ConstantConditions")
                String fileId = message.getPhoto().stream().max((Comparator.comparingInt(PhotoSize::getFileSize))).get().getFileId();
                try {
                    Path tempFile = Files.createTempFile(fileId, "tmp");
                    FileUtils.copyURLToFile(new URL(sender.execute(new GetFile().setFileId(fileId)).getFileUrl(sender.getBotToken())), tempFile.toFile());
                    text = ocrProvider.read(com.google.common.io.Files.toByteArray(new File(tempFile.toAbsolutePath().toString())));

                } catch (TelegramApiException | IOException | OCRException e) {
                    e.printStackTrace();
                    return;
                }
            } else {
                text = message.getText();
            }
            if (arguments[0].equals("kill")) {
                pandoraWebsitePoster.postKillCode(pandoraWebsitePoster.acquireKillCodeFromText(text), arguments);
            } else if (arguments[0].equals("puzzle")) {
                pandoraWebsitePoster.postPuzzleCode(pandoraWebsitePoster.acquirePuzzleCodeFromText(text), arguments);
            } else {
                return;
            }
        } else if (arguments[0].equals("help")) {
            SendMessage sendMessage = new SendMessage();

            InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();

            List<InlineKeyboardButton> buttonRow0 = new ArrayList<>();
            List<InlineKeyboardButton> buttonRow1 = new ArrayList<>();

            List<List<InlineKeyboardButton>> buttonRows = new ArrayList<>();

            InlineKeyboardButton buttonKill = new InlineKeyboardButton();
            buttonKill.setText("Enter Kill");
            buttonKill.setSwitchInlineQueryCurrentChat("kill");

            InlineKeyboardButton buttonPuzzle = new InlineKeyboardButton();
            buttonPuzzle.setText("Enter Puzzle");
            buttonPuzzle.setSwitchInlineQueryCurrentChat("puzzle");

            buttonRow0.add(buttonKill);
            buttonRow1.add(buttonPuzzle);
            buttonRows.add(buttonRow0);
            buttonRows.add(buttonRow1);

            keyboardMarkup.setKeyboard(buttonRows);

            sendMessage.setChatId(message.getChatId());
            sendMessage.setText("Loser");
            sendMessage.setReplyMarkup(keyboardMarkup);
            try {
                sender.execute(sendMessage);
            } catch (TelegramApiException e) {
                e.printStackTrace();
                return;
            }
        }
    }
}
