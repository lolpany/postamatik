package lol.lolpany.postamatik.youtube;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.SelenideElement;
import lol.lolpany.Account;
import lol.lolpany.postamatik.Content;
import lol.lolpany.postamatik.LocationOutputStream;
import lol.lolpany.postamatik.PostAction;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.io.IOException;

import static com.codeborne.selenide.Selenide.*;
import static com.codeborne.selenide.WebDriverRunner.closeWebDriver;
import static com.codeborne.selenide.WebDriverRunner.setWebDriver;
import static lol.lolpany.postamatik.SelenideUtils.waitTill;

public class YoutubeSelenideOutputStream implements LocationOutputStream {

    private final static String UPLOAD_BUTTON_SELECTOR = "div#upload-prompt-box input[type=\"file\"]";
    private final static int UPLOAD_SPEED = 1024;

    private final String chromeDriverLocation;
    private final Account account;
    private final YoutubeLocation location;

    public YoutubeSelenideOutputStream(String chromeDriverLocation, Account account, YoutubeLocation location) {
        this.chromeDriverLocation = chromeDriverLocation;
        this.account = account;
        this.location = location;

    }

    @Override
    public PostAction write(Content content) throws IOException {
        System.setProperty("webdriver.chrome.driver", chromeDriverLocation);
        ChromeOptions chromeOptions = new ChromeOptions();
        chromeOptions.addArguments("headless");
        setWebDriver(new ChromeDriver(chromeOptions));

        YoutubeUtils.authorize(account, location);

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
        return new YoutubeSelenidePostAction(chromeDriverLocation, account, location, videoId);
    }
}
