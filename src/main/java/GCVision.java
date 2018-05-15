import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.vision.v1.Vision;
import com.google.api.services.vision.v1.VisionScopes;
import com.google.api.services.vision.v1.model.*;
import com.google.common.collect.ImmutableList;
import com.google.gson.Gson;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by simon on 02.05.17.
 * Adaptet by Niels Overkamp on 09.03.18
 */
public class GCVision implements OCRProvider {

    private final Gson gson = new Gson();
    private HashMap<String, String> config = new HashMap<>();
    private Vision vision;

    GCVision() throws OCRException {
        try {
            GoogleCredential credential = GoogleCredential.getApplicationDefault().createScoped(VisionScopes.all());

            JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
            vision = new Vision.Builder(GoogleNetHttpTransport.newTrustedTransport(), jsonFactory, credential)
                    .setApplicationName("Telegram Bot")
                    .build();
        } catch (GeneralSecurityException | IOException e) {
            throw new OCRException("There was an error while initializing Google Butt Vision");
        }
    }

    @Override
    public String read(byte[] imageByteArray) throws OCRException {
        AnnotateImageRequest request =
                new AnnotateImageRequest()
                        .setImage(new Image().encodeContent(imageByteArray))
                        .setFeatures(ImmutableList.of(new Feature().setType("TEXT_DETECTION").setMaxResults(20)));
        try {
            Vision.Images.Annotate annotate =
                    vision.images()
                            .annotate(new BatchAnnotateImagesRequest().setRequests(ImmutableList.of(request)));
            List<AnnotateImageResponse> responses = annotate.execute().getResponses();
            return responses.stream().map(a -> a.getFullTextAnnotation().getText()).collect(Collectors.joining("\n"));
        } catch (IOException e) {
            throw new OCRException("There was an IO error while getting a request from Google Butt Vision");
        }

    }
}
