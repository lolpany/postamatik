package lol.lolpany.postamatik.pornhub;

import com.codeborne.selenide.Configuration;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.Channel;
import com.google.api.services.youtube.model.Video;
import lol.lolpany.Account;
import lol.lolpany.Location;
import lol.lolpany.postamatik.Content;
import lol.lolpany.postamatik.LocationTimelineReader;
import lol.lolpany.postamatik.Post;
import lol.lolpany.postamatik.PostState;
import lol.lolpany.postamatik.youtube.YoutubeApi;
import lol.lolpany.postamatik.youtube.YoutubeDesignation;
import lol.lolpany.postamatik.youtube.YoutubePostAction;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;

import static java.lang.Thread.sleep;
import static lol.lolpany.postamatik.Postamatik.CHROME_DRIVER_LOCATION;
import static lol.lolpany.postamatik.Postamatik.HEADLESS;
import static lol.lolpany.postamatik.pornhub.PornhubUtils.login;
import static org.openqa.selenium.By.cssSelector;

public class PornhubTimelineReader implements LocationTimelineReader {
    @Override
    public ConcurrentLinkedQueue<Post> read(Account account, Location location) {
        ConcurrentLinkedQueue<Post> result = new ConcurrentLinkedQueue<>();
        System.setProperty("webdriver.chrome.driver", CHROME_DRIVER_LOCATION);
        Configuration.timeout = 60000;
        ChromeOptions chromeOptions = new ChromeOptions();
        chromeOptions.addArguments("--no-sandbox");
        if (HEADLESS) {
            chromeOptions.setHeadless(true);
        }
        WebDriver webDriver = new ChromeDriver(chromeOptions);
        try {
            Instant postPseudoTime = Instant.now();
            int page = 1;
            login(webDriver, account);
            while (true) {
                webDriver.get("https://rt.pornhub.com/video/manage?page=" + page);
                sleep(5000);

                List<WebElement> videos = webDriver
                        .findElements(cssSelector("#videoManagerList .videoManageListItem .title"));
                if (videos.isEmpty()) {
                    break;
                }
                for (WebElement anchor : videos) {
                    Content content = new Content(null, null, null);
                    content.name = anchor.getText();
                    Post post = new Post(postPseudoTime, content, account, location);
                    post.postState = PostState.POSTED;
                    result.add(post);
                }
                page++;
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            webDriver.quit();
        }
        return result;
    }
}
