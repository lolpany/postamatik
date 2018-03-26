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
import lol.lolpany.postamatik.Location;
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
import static lol.lolpany.postamatik.ContentStreamerDispatcher.CHROME_DRIVER_LOCATION;
import static lol.lolpany.postamatik.SelenideUtils.waitTill;

public class YoutubeUtils {

    private final static String UPLOAD_BUTTON_SELECTOR = "div#upload-prompt-box input[type=\"file\"]";

    public static YouTube fetchYouTube(Account account, YoutubeLocation location) throws IOException, GeneralSecurityException {

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

    public static void authorize(Account account, YoutubeLocation location) {
        open("https://accounts.google.com/ServiceLogin/identifier?service=youtube&flowName=GlifWebSignIn&flowEntry=AddSession");
        if ($("input#identifierId").is(Condition.exist)) {
            $("input#identifierId").sendKeys(account.login);
            $("div#identifierNext").click();
            $("div#password input[type=\"password\"]").sendKeys(account.password);
            $("div#passwordNext").click();
        } else if ($("input#Email").exists()) {
            $("input#Email").sendKeys(account.login);
            $("input#next").click();
            $("input#Passwd").sendKeys(account.password);
            $("input#signIn").click();
        } else if ($("input[type=email]").exists()) {
            $("input[type=email]").sendKeys(account.login);
            $("div#identifierNext").click();
            $("input[type=password]").sendKeys(account.password);
            $("div#passwordNext").click();
        }

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

    public static String fetchAuthorizationCode(AuthorizationCodeRequestUrl authorizationCodeRequestUrl,
                                                Account account, YoutubeLocation location)
            throws MalformedURLException {
        System.setProperty("webdriver.chrome.driver", CHROME_DRIVER_LOCATION);
        ChromeOptions chromeOpts = new ChromeOptions();
        chromeOpts.addArguments("headless");
        setWebDriver(new ChromeDriver(chromeOpts));
        authorize(account, location);
        open(authorizationCodeRequestUrl.build().toString());

        while (!url().contains("/signin/oauth/oauthchooseaccount")
                && !url().contains("/signin/oauth/delegation")
                && !url().contains("DelegateAccountSelector")
                && !url().startsWith("http://www.example.com")
                && !$("form #submit_approve_access").is(Condition.visible)) {
            sleep(1000);
        }

        if (!url().startsWith("http://www.example.com")) {
            String accountsSelector = $("form ul").exists() ? "form ul li" : "ol#account-list li";
            for (SelenideElement accountLi : $$(accountsSelector)) {
                if (accountLi.innerText().contains(location.channelName)) {
                    accountLi.click();
                    break;
                }
            }

            while (!$("form #submit_approve_access").exists() && !url().startsWith("http://www.example.com")) {
                sleep(1000);
            }

            if ($("form #submit_approve_access").exists()) {
                while (!$("form #submit_approve_access").isEnabled()) {
                    sleep(1000);
                }
                $("form #submit_approve_access").click();
            }
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
