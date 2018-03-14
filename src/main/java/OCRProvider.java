import java.io.File;
import java.nio.file.Path;

public interface OCRProvider {

    String read(byte[] imageByteArray) throws OCRException;
}
