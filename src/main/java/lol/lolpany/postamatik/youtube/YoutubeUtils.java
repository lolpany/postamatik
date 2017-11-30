package lol.lolpany.postamatik.youtube;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.SelenideElement;
import com.google.api.client.auth.oauth2.*;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.youtube.YouTube;
import lol.lolpany.postamatik.Account;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.util.ArrayList;

import static com.codeborne.selenide.Selenide.*;
import static com.codeborne.selenide.WebDriverRunner.*;
import static lol.lolpany.postamatik.SelenideUtils.waitTill;

public class YoutubeUtils {

    private final static String UPLOAD_BUTTON_SELECTOR = "div#upload-prompt-box input[type=\"file\"]";


    public static YouTube fetchYouTube(Account account) throws IOException, GeneralSecurityException {

        AuthorizationCodeFlow authorizationCodeFlow = new AuthorizationCodeFlow.Builder(
                BearerToken.authorizationHeaderAccessMethod(),
                new NetHttpTransport(),
                new JacksonFactory(),
                new GenericUrl("https://accounts.google.com/o/oauth2/token"),
                new ClientParametersAuthentication(
                        "917439087874-rc9q2c1mb5mv8c2p5fe69errjeqmskvt.apps.googleusercontent.com",
                        "CCO7zqjHXl67GU1HhH4QDeip"),
                "917439087874-rc9q2c1mb5mv8c2p5fe69errjeqmskvt.apps.googleusercontent.com",
                "https://accounts.google.com/o/oauth2/v2/auth").setCredentialDataStore(
                StoredCredential.getDefaultDataStore(
                        new FileDataStoreFactory(new File("D:\\storage\\info\\buffer\\postamatik\\access-token"))))
                .build();

        Credential credential = authorizationCodeFlow.loadCredential(account.login);
        if (credential == null) {
            AuthorizationCodeRequestUrl authorizationCodeRequestUrl = authorizationCodeFlow.newAuthorizationUrl();
            authorizationCodeRequestUrl.setScopes(new ArrayList<String>() {{
                add("https://www.googleapis.com/auth/youtube.upload");
            }});
            authorizationCodeRequestUrl.setRedirectUri("http://www.example.com");
            String authorizationCode = fetchAuthorizationCode(authorizationCodeRequestUrl);
            AuthorizationCodeTokenRequest tokenRequest = authorizationCodeFlow.newTokenRequest(authorizationCode);
            tokenRequest.setRedirectUri("http://www.example.com");
            credential = authorizationCodeFlow.createAndStoreCredential(tokenRequest.execute(), account.login);
        }

        return new YouTube.Builder(
                GoogleNetHttpTransport.newTrustedTransport(), JacksonFactory.getDefaultInstance(), credential)
                .setApplicationName("postamatik")
                .build();
    }

    public static void authorize(Account account, YoutubeLocation location) {
        open("https://accounts.google.com/ServiceLogin/identifier?service=youtube&flowName=GlifWebSignIn&flowEntry=AddSession");
        $("input#identifierId").sendKeys(account.login);
        $("div#identifierNext").click();
        $("div#password input[type=\"password\"]").sendKeys(account.password);
        $("div#passwordNext").click();
        waitTill(() -> url().startsWith("https://myaccount.google.com/")
                || $("li.identity-prompt-account-list-item").exists());

        open("https://www.youtube.com/upload");
        waitTill(() -> /*$("li.identity-prompt-account-list-item").exists() || */$(UPLOAD_BUTTON_SELECTOR).exists());

        if ($("li.identity-prompt-account-list-item").exists()) {
            for (SelenideElement accountLi : $$("li.identity-prompt-account-list-item")) {
                if (accountLi.innerText().contains(location.channelName)) {
                    accountLi.click();
                    break;
                }
            }
            $("button#identity-prompt-confirm-button").click();
        }
        $(UPLOAD_BUTTON_SELECTOR).should(Condition.exist);
    }

    public static String fetchAuthorizationCode(AuthorizationCodeRequestUrl authorizationCodeRequestUrl) throws MalformedURLException {
        System.setProperty("webdriver.chrome.driver", "D:\\buffer\\chromedriver\\chromedriver.exe");
        ChromeOptions chromeOpts = new ChromeOptions();
        chromeOpts.addArguments("headless");
        setWebDriver(new ChromeDriver(chromeOpts));
        authorize(new Account(null, "funnymeatworld@gmail.com", "As123456", null), new YoutubeLocation(null, null,
                "supergame"));
        open(authorizationCodeRequestUrl.build().toString());

        if ($("form ul").exists()) {
            for (SelenideElement accountLi : $$("form ul li")) {
                if (accountLi.innerText().contains("supergame")) {
                    accountLi.click();
                    break;
                }
            }
        }

        if ($("form div#submit_approve_access").exists()) {
            $("form div#submit_approve_access").click();
        }

        while (!url().startsWith("http://www.example.com")) {
            sleep(1000);
        }

        String result = new URL(url()).getQuery().split("=")[1];
        close();
        closeWebDriver();
        return result;
    }


}
