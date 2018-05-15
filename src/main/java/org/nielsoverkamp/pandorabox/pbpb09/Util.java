package org.nielsoverkamp.pandorabox.pbpb09;

import org.telegram.telegrambots.api.objects.Message;

public class Util {

    public static String getTextFromMessage(Message message) {
        if(message.hasText()) {
            return message.getText();
        } else if (message.hasPhoto()) {
            return message.getCaption();
        }
        return null;
    }

    public static String[] split(String text) {
        return text.split("\\s+");
    }


}
