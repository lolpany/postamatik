package lol.lolpany.postamatik;

import com.google.api.client.auth.oauth2.*;
import com.google.api.client.util.store.DataStore;
import com.google.api.client.util.store.FileDataStoreFactory;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

public class TestApi {

    private final static String USER_ID = "postamatik";

    @Test
    public void go() throws IOException {

//        AuthorizationCodeFlow authorizationCodeFlow = new AuthorizationCodeFlow(, , , ,, , );
//
//        Credential credential = authorizationCodeFlow.loadCredential(USER_ID);
//        if (credential == null) {
//            AuthorizationCodeRequestUrl authorizationCodeRequestUrl = authorizationCodeFlow.newAuthorizationUrl();
//            authorizationCodeFlow.newTokenRequest(code);
//            credential = authorizationCodeFlow.createAndStoreCredential(response, USER_ID);
//        }
//
//
//                Credential credential =
//                new Credential(BearerToken.authorizationHeaderAccessMethod()).setAccessToken(accessToken);
//        DataStore<StoredCredential> dataStore =
//                new FileDataStoreFactory(new File("D:\\storage\\info\\buffer\\postamatik\\access-token"))
//                        .getDataStore("postamatik");
    }
}
