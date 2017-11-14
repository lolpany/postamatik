package lol.lolpany.postamatik;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.SelenideElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.io.File;
import java.io.IOException;

import static com.codeborne.selenide.Selenide.*;
import static com.codeborne.selenide.WebDriverRunner.closeWebDriver;
import static com.codeborne.selenide.WebDriverRunner.setWebDriver;
import static com.codeborne.selenide.WebDriverRunner.url;
import static lol.lolpany.postamatik.SelenideUtils.waitTill;

public class YoutubeOutputStream implements LocationOutputStream {

    private final static String UPLOAD_BUTTON_SELECTOR = "div#upload-prompt-box input[type=\"file\"]";
    private final static int UPLOAD_SPEED = 1024;

    private final String chromeDriverLocation;
    private final Account account;
    private final YoutubeLocation location;

    public YoutubeOutputStream(String chromeDriverLocation, Account account, YoutubeLocation location) {
        this.chromeDriverLocation = chromeDriverLocation;
        this.account = account;
        this.location = location;
        Configuration.timeout = 10000;
    }

    @Override
    public PostAction write(Content content) throws IOException {
        System.setProperty("webdriver.chrome.driver", chromeDriverLocation);
        ChromeOptions chromeOptions = new ChromeOptions();
        chromeOptions.addArguments("headless");
        setWebDriver(new ChromeDriver(chromeOptions));

        authorize(account, location);

        open("https://www.youtube.com/upload");

        $(UPLOAD_BUTTON_SELECTOR).should(Condition.exist);

        $("div#upload-prompt-box input[type=\"file\"]").uploadFile(content.file);



        SelenideElement title = $("div#main-content input.video-settings-title");
        title.clear();
        title.sendKeys(content.name);

        SelenideElement activeUploadsDiv = $("div#active-uploads-contain");
        SelenideElement uploadPercent = activeUploadsDiv.find("div.progress-bar-uploading span.progress-bar-percentage");
        SelenideElement processBar = activeUploadsDiv.find("div.progress-bar-progress");
        SelenideElement uploadFailure = activeUploadsDiv.find("div.upload-failure");

        waitTill(
                () -> ((uploadPercent.innerText().equals("100%") || uploadPercent.innerText().equals("100 %"))
                        || (uploadFailure.is(Condition.visible)
                        && (uploadFailure.innerText().equals("Загрузка завершена.")
                        || uploadFailure.innerText().equals("The upload has finished.")))
                ),
                content.file.length() / UPLOAD_SPEED);



        $("div#active-uploads-contain div.watch-page-link > a").should(Condition.exist);

        String videoUrl = $("div#active-uploads-contain div.watch-page-link > a").text();
        String videoId = videoUrl.substring(videoUrl.lastIndexOf("/") + 1);
        close();
        closeWebDriver();
        return new YoutubePostAction(chromeDriverLocation, account, location, videoId);
    }

    static void authorize(Account account, YoutubeLocation location) {
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
}
