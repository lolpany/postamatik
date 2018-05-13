package lol.lolpany.postamatik.youtube;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.SelenideElement;
import com.google.api.client.auth.oauth2.*;
import lol.lolpany.Account;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.net.MalformedURLException;
import java.net.URL;

import static com.codeborne.selenide.Selenide.*;
import static com.codeborne.selenide.WebDriverRunner.*;
import static lol.lolpany.postamatik.ContentStreamerDispatcher.CHROME_DRIVER_LOCATION;
import static lol.lolpany.postamatik.SelenideUtils.waitTill;

public class YoutubeUtils {

    public static final int FETCH_SIZE = 50;
    private final static String UPLOAD_BUTTON_SELECTOR = "div#upload-prompt-box input[type=\"file\"]";

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
        } else if ($("#identity-prompt-dialog").exists()) {
            $(".specialized-identity-prompt-account-item page main").click();
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
