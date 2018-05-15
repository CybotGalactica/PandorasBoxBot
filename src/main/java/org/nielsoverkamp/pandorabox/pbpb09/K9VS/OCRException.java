package org.nielsoverkamp.pandorabox.pbpb09.K9VS;

public class OCRException extends Exception {
    private final String message;

    public OCRException(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "OCR Exception: " + message;
    }
}
