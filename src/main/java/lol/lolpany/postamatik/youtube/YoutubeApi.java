package lol.lolpany.postamatik.youtube;

import com.google.api.client.auth.oauth2.*;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.YouTubeRequest;
import lol.lolpany.postamatik.Account;
import lol.lolpany.postamatik.Location;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static lol.lolpany.postamatik.youtube.YoutubeUtils.fetchAuthorizationCode;

public class YoutubeApi {

    private Map<String, Map<String, YouTube>> youTubeByChannelByAccount;

    private YoutubeApi() {
    }

    public <T> T execute(Account account, YoutubeLocation location, YouTubeRequest<T> request) throws IOException, GeneralSecurityException {
        YouTube youTube = null;
        if (youTubeByChannelByAccount.get(account.login) != null) {
            youTube = youTubeByChannelByAccount.get(account.login).get(location.url.toString());
        }
        if (youTube == null) {
            youTube = fetchYouTube(account, location);
            youTubeByChannelByAccount.computeIfAbsent(account.login, key -> new ConcurrentHashMap<>())
                    .put(location.url.toString(), youTube);
        }
        try {
            return request.execute();
        } catch (GoogleJsonResponseException e) {
            if (e.getStatusCode() == 401) {
//                youTube = ;
            }
        }
        return null;
    }

    private static YouTube fetchYouTube(Account account, YoutubeLocation location) throws IOException, GeneralSecurityException {

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

        Credential credential = authorizationCodeFlow.loadCredential(account.login);
        if (credential == null) {
            AuthorizationCodeRequestUrl authorizationCodeRequestUrl = authorizationCodeFlow.newAuthorizationUrl();
            authorizationCodeRequestUrl.setScopes(new ArrayList<String>() {{
                add("https://www.googleapis.com/auth/youtube");
                add("https://www.googleapis.com/auth/youtube.upload");
            }});
            authorizationCodeRequestUrl.setRedirectUri("http://www.example.com");
            String authorizationCode =
                    java.net.URLDecoder.decode(fetchAuthorizationCode(authorizationCodeRequestUrl, account, location),
                            "UTF-8");
            AuthorizationCodeTokenRequest tokenRequest = authorizationCodeFlow.newTokenRequest(authorizationCode);
            tokenRequest.setRedirectUri("http://www.example.com");
            credential = authorizationCodeFlow.createAndStoreCredential(tokenRequest.execute(), account.login);
        }

        return new YouTube.Builder(
                GoogleNetHttpTransport.newTrustedTransport(), JacksonFactory.getDefaultInstance(), credential)
                .setApplicationName("postamatik")
                .build();
    }


}
