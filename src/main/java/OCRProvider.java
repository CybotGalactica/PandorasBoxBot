public interface OCRProvider {

    String read(byte[] imageByteArray) throws OCRException;
}
