package lol.lolpany.postamatik.youtube;

import com.google.api.client.auth.oauth2.*;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.googleapis.media.MediaHttpUploader;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.InputStreamContent;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.Video;
import com.google.api.services.youtube.model.VideoSnippet;
import com.google.api.services.youtube.model.VideoStatus;
import lol.lolpany.Account;
import org.junit.Test;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import static lol.lolpany.postamatik.youtube.YoutubeUtils.fetchAuthorizationCode;

public class TestApi {

    private final static String USER_ID = "postamatik";

    @Test
    public void go() throws IOException, GeneralSecurityException {

        AuthorizationCodeFlow authorizationCodeFlow = new AuthorizationCodeFlow.Builder(
                BearerToken.authorizationHeaderAccessMethod(),
                new NetHttpTransport(),
                new JacksonFactory(),
                new GenericUrl("https://accounts.google.com/o/oauth2/token"),
                new ClientParametersAuthentication(
                        "917439087874-rc9q2c1mb5mv8c2p5fe69errjeqmskvt.apps.googleusercontent.com",
                        "CCO7zqjHXl67GU1HhH4QDeip"),
                "917439087874-rc9q2c1mb5mv8c2p5fe69errjeqmskvt.apps.googleusercontent.com",
                "https://accounts.google.com/o/oauth2/v2/auth")
//                .setCredentialDataStore(
//                StoredCredential.getDefaultDataStore(
//                        new FileDataStoreFactory(new File("D:\\storage\\info\\buffer\\postamatik\\access-token"))))
                .build();

        Credential credential = authorizationCodeFlow.loadCredential(USER_ID);
        if (credential == null) {
            AuthorizationCodeRequestUrl authorizationCodeRequestUrl = authorizationCodeFlow.newAuthorizationUrl();
            authorizationCodeRequestUrl.setScopes(new ArrayList<String>() {{
                add("https://www.googleapis.com/auth/youtube");
                add("https://www.googleapis.com/auth/youtube.upload");
            }});
            authorizationCodeRequestUrl.setRedirectUri("http://www.example.com");
            String authorizationCode = fetchAuthorizationCode(authorizationCodeRequestUrl,
                    new Account(null,null,null,null), new YoutubeLocation(null, null, "supergame"));
            AuthorizationCodeTokenRequest tokenRequest = authorizationCodeFlow.newTokenRequest(authorizationCode);
            tokenRequest.setRedirectUri("http://www.example.com");
            credential = authorizationCodeFlow.createAndStoreCredential(tokenRequest.execute(), USER_ID);
        }


        YouTube youTube = new YouTube.Builder(
                GoogleNetHttpTransport.newTrustedTransport(), JacksonFactory.getDefaultInstance(), credential)
                .setApplicationName("postamatik")
                .build();


        com.google.api.services.youtube.model.Video videoObjectDefiningMetadata = new com.google.api.services.youtube.model.Video();

        // Set the video to be publicly visible. This is the default
        // setting. Other supporting settings are "unlisted" and "private."
        VideoStatus status = new VideoStatus();
        status.setPrivacyStatus("private");
//        status.setPublishAt(new DateTime(new Date(new Date().getTime() + 360000 * 1000)));
        videoObjectDefiningMetadata.setStatus(status);

        // Most of the video's metadata is set on the VideoSnippet object.
        VideoSnippet snippet = new VideoSnippet();

        // This code uses a Calendar instance to create a unique name and
        // description for test purposes so that you can easily upload
        // multiple files. You should remove this code from your project
        // and use your own standard names instead.
        Calendar cal = Calendar.getInstance();
        snippet.setTitle("Test Upload via Java on " + cal.getTime());
        snippet.setDescription(
                "Video uploaded via YouTube Data API V3 using the Java library " + "on " + cal.getTime());

        // Set the keyword tags that you want to associate with the video.
        List<String> tags = new ArrayList<String>();
        tags.add("test");
        tags.add("example");
        tags.add("java");
        tags.add("YouTube Data API V3");
        tags.add("erase me");
        snippet.setTags(tags);

        // Add the completed snippet object to the video resource.
        videoObjectDefiningMetadata.setSnippet(snippet);

        InputStreamContent mediaContent = new InputStreamContent("video/*",
                new FileInputStream("D:\\buffer\\go.mp4"));

        // Insert the video. The command sends three arguments. The first
        // specifies which information the API request is setting and which
        // information the API response should return. The second argument
        // is the video resource that contains metadata about the new video.
        // The third argument is the actual video content.
        YouTube.Videos.Insert videoInsert = youTube.videos()
                .insert("snippet,statistics,status", videoObjectDefiningMetadata, mediaContent);

        // Set the upload type and add an event listener.
        MediaHttpUploader uploader = videoInsert.getMediaHttpUploader();
        uploader.setDisableGZipContent(true);

        // Indicate whether direct media upload is enabled. A value of
        // "True" indicates that direct media upload is enabled and that
        // the entire media content will be uploaded in a single request.
        // A value of "False," which is the default, indicates that the
        // request will use the resumable media upload protocol, which
        // supports the ability to resume an upload operation after a
        // network interruption or other transmission failure, saving
        // time and bandwidth in the event of network failures.
        uploader.setDirectUploadEnabled(false);


//        MediaHttpUploaderProgressListener progressListener = new MediaHttpUploaderProgressListener() {
//            public void progressChanged(MediaHttpUploader uploader) throws IOException {
//                switch (uploader.getUploadState()) {
//                    case INITIATION_STARTED:
//                        System.out.println("Initiation Started");
//                        break;
//                    case INITIATION_COMPLETE:
//                        System.out.println("Initiation Completed");
//                        break;
//                    case MEDIA_IN_PROGRESS:
//                        System.out.println("Upload in progress");
//                        System.out.println("Upload percentage: " + uploader.getProgress());
//                        break;
//                    case MEDIA_COMPLETE:
//                        System.out.println("Upload Completed!");
//                        break;
//                    case NOT_STARTED:
//                        System.out.println("Upload Not Started!");
//                        break;
//                }
//            }
//        };
//        uploader.setProgressListener(progressListener);

        // Call the API and upload the video.
        com.google.api.services.youtube.model.Video returnedVideo = videoInsert.execute();

        System.out.println(returnedVideo.getId());

//        YouTube.Videos.List list = youTube.videos().list("id").setId(returnedVideo.getId())

        youTube.videos().update("status", new Video().setId(returnedVideo.getId()).setStatus(
                new VideoStatus().setPrivacyStatus("public")
        )).execute();

    }
}
