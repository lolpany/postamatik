package lol.lolpany.postamatik.youtube;

import com.codeborne.selenide.Condition;
import lol.lolpany.postamatik.Account;
import lol.lolpany.postamatik.PostAction;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import static com.codeborne.selenide.Selenide.*;
import static com.codeborne.selenide.WebDriverRunner.closeWebDriver;
import static com.codeborne.selenide.WebDriverRunner.setWebDriver;

public class YoutubeSelenidePostAction implements PostAction {
    final String chromeDriverLocation;
    final Account account;
    final YoutubeLocation location;
    final String videoId;

    public YoutubeSelenidePostAction(String chromeDriverLocation, Account account, YoutubeLocation location, String videoId) {
        this.chromeDriverLocation = chromeDriverLocation;
        this.account = account;
        this.location = location;
        this.videoId = videoId;
    }

    @Override
    public void run() {
        System.setProperty("webdriver.chrome.driver", chromeDriverLocation);
        ChromeOptions chromeOpts = new ChromeOptions();
        chromeOpts.addArguments("headless");
        setWebDriver(new ChromeDriver(chromeOpts));
        YoutubeUtils.authorize(account, location);
        open("https://www.youtube.com/edit?o=U&video_id=" + videoId);

        $("div.save-cancel-buttons button.save-changes-button").should(Condition.exist).scrollTo().click();

        close();
        closeWebDriver();
    }
}
