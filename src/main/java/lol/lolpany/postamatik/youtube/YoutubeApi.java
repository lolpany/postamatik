package lol.lolpany.postamatik.youtube;

import com.google.api.client.auth.oauth2.*;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeRequestUrl;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.YouTubeRequest;
import lol.lolpany.postamatik.Account;
import lol.lolpany.postamatik.Location;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static lol.lolpany.postamatik.youtube.YoutubeUtils.fetchAuthorizationCode;

public class YoutubeApi {

    private static final String CREDENTIAL_STORAGE = "D:\\storage\\info\\buffer\\postamatik\\access-token";
    private static final String CLIENT_ID = "917439087874-rc9q2c1mb5mv8c2p5fe69errjeqmskvt.apps.googleusercontent.com";
    private static final String AUTHORIZATION_SERVER_ENCODED_URL = "https://accounts.google.com/o/oauth2/v2/auth";
    private static final String CLIENT_SECRET = "CCO7zqjHXl67GU1HhH4QDeip";

    private Map<String, Map<String, YouTube>> youTubeByChannelByAccount;

    private YoutubeApi() {
    }

    public static YouTube fetchYouTube(Account account, YoutubeLocation location) throws IOException, GeneralSecurityException {

        GoogleAuthorizationCodeFlow authorizationCodeFlow = new GoogleAuthorizationCodeFlow.Builder(
//                BearerToken.authorizationHeaderAccessMethod(),
                new NetHttpTransport(),
                new JacksonFactory(),
                CLIENT_ID, CLIENT_SECRET, new ArrayList<String>() {
            {
                add("https://www.googleapis.com/auth/youtube");
                add("https://www.googleapis.com/auth/youtube.upload");
            }
        }
//                new GenericUrl("https://accounts.google.com/o/oauth2/token"),
//                new ClientParametersAuthentication(
//                        CLIENT_ID, "CCO7zqjHXl67GU1HhH4QDeip"
//                ), CLIENT_ID, AUTHORIZATION_SERVER_ENCODED_URL
        ).setAccessType("offline").setApprovalPrompt("force").setCredentialDataStore(
                StoredCredential.getDefaultDataStore(
                        new FileDataStoreFactory(new File(CREDENTIAL_STORAGE))))
                .build();

        Credential credential = authorizationCodeFlow.loadCredential(account.login);
        if (credential == null) {
//            AuthorizationCodeRequestUrl authorizationCodeRequestUrl = authorizationCodeFlow.newAuthorizationUrl();
//            authorizationCodeRequestUrl.setScopes();
//        authorizationCodeRequestUrl.setRedirectUri("http://www.example.com");
            GoogleAuthorizationCodeRequestUrl codeRequestUrl = authorizationCodeFlow.newAuthorizationUrl().setRedirectUri("http://www.example.com");
//        (AUTHORIZATION_SERVER_ENCODED_URL, CLIENT_ID, "http://www.example.com",
//                        new ArrayList<String>() {{
//                            add("https://www.googleapis.com/auth/youtube");
//                            add("https://www.googleapis.com/auth/youtube.upload");
//                        }});
//                codeRequestUrl.setAccessType("offline");
//                codeRequestUrl.setApprovalPrompt("force");
            String authorizationCode =
                    java.net.URLDecoder.decode(fetchAuthorizationCode(codeRequestUrl, account, location),
                            "UTF-8");
            AuthorizationCodeTokenRequest tokenRequest = authorizationCodeFlow.newTokenRequest(authorizationCode).setRedirectUri("http://www.example.com").setGrantType("authorization_code");
//        tokenRequest;

            credential = authorizationCodeFlow.createAndStoreCredential(tokenRequest.execute(), account.login);
//        }

//                if (credential.getRefreshToken() == null) {
//                    credential.setRefreshToken(authorizationCodeFlow.newTokenRequest(authorizationCode).setGrantType("refresh_token").execute().getRefreshToken()).setAccessToken(credential.getAccessToken());
//                }
        }

        return new YouTube.Builder(
                GoogleNetHttpTransport.newTrustedTransport(), JacksonFactory.getDefaultInstance(), credential)
                .setApplicationName("postamatik")
                .build();
    }

//    public <T> T execute(Account account, YoutubeLocation location, YouTubeRequest<T> request) throws IOException, GeneralSecurityException {
//        YouTube youTube = null;
//        if (youTubeByChannelByAccount.get(account.login) != null) {
//            youTube = youTubeByChannelByAccount.get(account.login).get(location.url.toString());
//        }
//        if (youTube == null) {
//            youTube = fetchYouTube(account, location);
//            youTubeByChannelByAccount.computeIfAbsent(account.login, key -> new ConcurrentHashMap<>())
//                    .put(location.url.toString(), youTube);
//        }
//        try {
//            return request.execute();
//        } catch (GoogleJsonResponseException e) {
//            if (e.getStatusCode() == 401) {
////                youTube = ;
//            }
//        }
//        return null;
//    }


}
