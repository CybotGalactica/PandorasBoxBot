package org.nielsoverkamp.pandorabox.pbpb09.K9VS;

public interface OCRProvider {

    String read(byte[] imageByteArray) throws OCRException;
}
