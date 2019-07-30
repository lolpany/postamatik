package lol.lolpany.postamatik.pornhub;

import com.codeborne.selenide.Configuration;
import lol.lolpany.Account;
import lol.lolpany.Location;
import lol.lolpany.postamatik.*;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Triple;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static lol.lolpany.postamatik.Postamatik.CHROME_DRIVER_LOCATION;
import static lol.lolpany.postamatik.Postamatik.HEADLESS;
import static lol.lolpany.postamatik.SelenideUtils.isDaysPassed;
import static org.apache.commons.lang3.StringUtils.trim;
import static org.openqa.selenium.By.cssSelector;

public class PornhubContentSearch implements ContentSearch {

    private final String url;
    private final Set<String> tags;

    PornhubContentSearch(String url, Set<String> tags) {
        this.url = url;
        this.tags = tags;
    }

    @Override
    public Content findContent(double precision, Set<String> tags, PostsTimeline postsTimeline, Account account,
                               Location<LocationConfig> location) throws InterruptedException {
        Content result = null;
        System.setProperty("webdriver.chrome.driver", CHROME_DRIVER_LOCATION);
        Configuration.timeout = 60000;
        ChromeOptions chromeOptions = new ChromeOptions();
        chromeOptions.addArguments("--no-sandbox");
        if (HEADLESS) {
            chromeOptions.setHeadless(true);
        }
        WebDriver webDriver = new ChromeDriver(chromeOptions);
        try {
            if (Utils.match(this.tags, tags) >= precision) {
                webDriver.get("https://pornhub.com/");
                webDriver.findElement(cssSelector("#js-checkAge")).click();
                for (String windowHandle :webDriver.getWindowHandles()) {
                    webDriver.switchTo().window(windowHandle);
                    if (webDriver.getTitle().contains("Вход")) {
                        break;
                    }
                }
                webDriver.findElement(cssSelector("input[name='email']")).sendKeys("79670207451");
                webDriver.findElement(cssSelector("input[name='pass']")).sendKeys("er234sddsf3");
                webDriver.findElement(cssSelector("#install_allow")).click();
                for (String windowHandle :webDriver.getWindowHandles()) {
                    webDriver.switchTo().window(windowHandle);
                    if (webDriver.getTitle().contains("Порно")) {
                        break;
                    }
                }
                for (int i = 1; i < 2000; i ++) {
                    webDriver.get(this.url + "&page=" + i);
                    Thread.sleep(5000);
                    result = findContent(webDriver, tags, postsTimeline, location);
                    if (result != null) {
                        break;
                    }
                }
            }
        } finally {
            webDriver.quit();
        }
        return result;

    }

    private Content findContent(WebDriver webDriver, Set<String> tags, PostsTimeline postsTimeline,
                                   Location<LocationConfig> location) throws InterruptedException {
        Content result = null;
        for (WebElement a : webDriver.findElements(cssSelector("#videoCategory a.linkVideoThumb"))) {
            String pageUrl = a.getAttribute("href");
            Content content = new Content(tags, singletonList(pageUrl), emptyList());
            if (!postsTimeline.isAlreadyScheduledOrUploadedOrPosted(location.url.toString(), content)) {
                result = content;
                break;
            }
        }
        return result;
    }

}