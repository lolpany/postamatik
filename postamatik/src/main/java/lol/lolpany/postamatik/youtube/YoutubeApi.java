package lol.lolpany.postamatik.youtube;

import com.google.api.client.auth.oauth2.AuthorizationCodeTokenRequest;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.StoredCredential;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeRequestUrl;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpBackOffIOExceptionHandler;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.youtube.YouTube;
import lol.lolpany.Account;
import org.apache.commons.io.FileUtils;

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
    private static final File CLIENT_SECRET = new File("D:\\storage\\info\\buffer\\postamatik\\clientSecret.txt");

    private static Map<String, Map<String, Credential>> credentialByChannelByAccount = new ConcurrentHashMap<>();

    private YoutubeApi() {
    }

    private static Credential initCredential(Account account, YoutubeLocation location) throws IOException, GeneralSecurityException {

        GoogleAuthorizationCodeFlow authorizationCodeFlow = new GoogleAuthorizationCodeFlow.Builder(
//                BearerToken.authorizationHeaderAccessMethod(),
                new NetHttpTransport(),
                new JacksonFactory(),
                CLIENT_ID, FileUtils.readFileToString(CLIENT_SECRET), new ArrayList<String>() {
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

        return credential;
    }

    public static YouTube fetchYouTube(Account account, YoutubeLocation location) throws IOException, GeneralSecurityException {
        Credential credential = null;
        if (credentialByChannelByAccount.get(account.login) != null) {
            credential = credentialByChannelByAccount.get(account.login).get(location.url.toString());
        }
        if (credential == null) {
            credential = initCredential(account, location);
            credentialByChannelByAccount.computeIfAbsent(account.login, key -> new ConcurrentHashMap<>())
                    .put(location.url.toString(), credential);
        }
        return new YouTube.Builder(
                GoogleNetHttpTransport.newTrustedTransport(), JacksonFactory.getDefaultInstance(), credential)
                .setApplicationName("postamatik")
                .build();
    }

//    public <T> T execute(Account account, YoutubeLocation location, YouTubeRequest<T> request) throws IOException, GeneralSecurityException {
//        YouTube youTube = null;
//        if (credentialByChannelByAccount.get(account.login) != null) {
//            youTube = credentialByChannelByAccount.get(account.login).get(location.url.toString());
//        }
//        if (youTube == null) {
//            youTube = fetchYouTube(account, location);
//            credentialByChannelByAccount.computeIfAbsent(account.login, key -> new ConcurrentHashMap<>())
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
