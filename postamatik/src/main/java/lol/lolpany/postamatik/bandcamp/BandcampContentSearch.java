package lol.lolpany.postamatik.bandcamp;

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

public class BandcampContentSearch implements ContentSearch {

    private final String url;
    private final Set<String> tags;

    BandcampContentSearch(String url, Set<String> tags) {
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
//            System.setProperty("webdriver.gecko.driver", GECKO_DRIVER_LOCATION);
//            FirefoxOptions firefoxOptions = new FirefoxOptions();
//            firefoxOptions.setAcceptInsecureCerts(true);
//            if (HEADLESS) {
//                firefoxOptions.setHeadless(true);
//            }
//            setWebDriver(new FirefoxDriver(firefoxOptions));

//            Configuration.baseUrl = "https://bandcamp.com";

            if (Utils.match(this.tags, tags) >= precision) {
                webDriver.get(this.url);
                Thread.sleep(5000);
                if (webDriver.findElements(cssSelector("div.follow")).isEmpty()) {
                    result = findContentOld(webDriver, tags, postsTimeline, location);
                } else {
                    result = findContentNew(webDriver, tags, postsTimeline, location);
                }
            }
        } finally {
            webDriver.quit();
        }
        return result;

    }

    private Content findContentOld(WebDriver webDriver, Set<String> tags, PostsTimeline postsTimeline,
                                   Location<LocationConfig> location) throws InterruptedException {
        Content result = null;
        for (int i = 1; i <= 10; i++) {
            String pageUrl = this.url + (this.url.contains("?") ? "&" : "?") + "page=" + i;
            webDriver.get(pageUrl);

            List<Triple<String, String, String>> albums = new ArrayList<>();
            for (WebElement albumItem : webDriver.findElements(cssSelector("li.item"))) {
                albums.add(new ImmutableTriple<>(albumItem.findElement(cssSelector("a")).getAttribute("href"),
                        albumItem.findElement(cssSelector("div.itemsubtext")).getText(),
                        albumItem.findElement(cssSelector("div.itemtext")).getText()));
            }
            for (Triple<String, String, String> album : albums) {
                result = extractContent(webDriver, postsTimeline, location, pageUrl, album);
                if (result != null) {
                    return result;
                }
            }

        }
        return result;
    }


    private Content findContentNew(WebDriver webDriver, Set<String> tags, PostsTimeline postsTimeline,
                                   Location<LocationConfig> location) throws InterruptedException {
        String pageUrl = this.url;
        webDriver.get(pageUrl);
        WebElement button = webDriver.findElement(cssSelector("button.view-more"));
        button.click();
        int i = 0;
        while (scrollMore(webDriver, 5)) {
            List<Triple<String, String, String>> albums = new ArrayList<>();
            for (WebElement albumItem : webDriver.findElements(cssSelector("div#dig-deeper div.dig-deeper-item"))) {
                albums.add(new ImmutableTriple<>(albumItem.findElement(cssSelector("a")).getAttribute("href"),
                        albumItem.findElement(cssSelector("div.artist > span")).getText(),
                        albumItem.findElement(cssSelector("div.title")).getText()));
            }
            for (Triple<String, String, String> album : albums.subList(i, albums.size())) {
                Content result = extractContent(webDriver, postsTimeline, location, pageUrl, album);
                if (result != null) {
                    return result;
                }
                i++;
            }
        }
        return null;
    }

    private Content extractContent(WebDriver webDriver, PostsTimeline postsTimeline, Location<LocationConfig> location,
                                   String pageUrl,
                                   Triple<String, String, String> album) throws InterruptedException {
        webDriver.get(album.getLeft());
        ContentLength contentLength = location.locationConfig.contentLengths
                .get(new Random().nextInt(location.locationConfig.contentLengths.size()));
        switch (contentLength) {
            case LONG:
            case MEDIUM:
                if (isMediumOrLongContentLengthSuitable(webDriver, location.locationConfig.contentLengths)) {
                    Content content = new Content(tags, singletonList(album.getLeft()), emptyList());
                    webDriver.get(pageUrl);
                    content.name = album.getMiddle() + " - " + album.getRight();
                    if (!postsTimeline.isAlreadyScheduledOrUploadedOrPosted(location.url.toString(), content)) {
                        return content;
                    }
                    content.time = Instant.now();
                    if (isDaysPassed(location.locationConfig, content)) {
                        return null;
                    }
                }
                break;
            case SHORT:
//                List<WebElement> timeSpans = webDriver.findElements(cssSelector("div.title > span"));
//                timeSpans.get(timeSpans.size() - 1).scrollTo();
                scrollMore(webDriver, 1);
                for (WebElement track : webDriver.findElements(cssSelector("div.title"))) {
                    if (isShortContentLengthSuitable(location.locationConfig.contentLengths, track)) {
                        Content content = new Content(tags,
                                singletonList(track.findElement(cssSelector("a")).getAttribute("href")), emptyList());
                        String trackLabel =
                                track.findElement(cssSelector("a")).findElement(cssSelector("span")).getText();
                        if (trackLabel.contains("-")) {
                            content.name = trackLabel;
                        } else {
                            content.name =
                                    webDriver.findElement(cssSelector("div#name-section span > a")).getText() + " - " +
                                            trackLabel;
                        }
                        if (!postsTimeline.isAlreadyScheduledOrUploadedOrPosted(location.url.toString(), content)) {
                            return content;
                        }
                        content.time = parseContentTime(webDriver);
                        if (isDaysPassed(location.locationConfig, content)) {
                            return null;
                        }
                    }
                }
        }
        return null;
    }

    private Instant parseContentTime(WebDriver webDriver) {
        String date = webDriver
                .findElement(cssSelector("#trackInfoInner > .tralbum-credits > meta[itemprop=\"datePublished\"]"))
                .getAttribute("content");
        return LocalDate.of(Integer.parseInt(date.substring(0, 4)), Integer.parseInt(date.substring(4, 6)),
                Integer.parseInt(date.substring(6, 8))).atStartOfDay().toInstant(
                ZoneOffset.UTC);
    }

    private boolean isShortContentLengthSuitable(List<ContentLength> locationContentLength, WebElement track) {
        if (!track.findElements(cssSelector("span.time")).isEmpty()) {
            return locationContentLength.contains(
                    ContentLength.fromMinutes(toMinutes(track.findElement(cssSelector("span.time")).getText())));
        } else {
            return false;
        }
    }

    private boolean scrollMore(WebDriver webDriver, int times) throws InterruptedException {
        int initialHeight = webDriver.findElement(cssSelector("body")).getSize().getHeight();
        int i = 0;
        while (initialHeight == webDriver.findElement(cssSelector("body")).getSize().getHeight() && i < times) {
            i++;
            ((JavascriptExecutor) webDriver)
                    .executeScript("window.scrollTo(0, document.body.scrollHeight)");
            Thread.sleep(5000);
            ((JavascriptExecutor) webDriver)
                    .executeScript("window.scrollTo(0, document.body.scrollHeight)");
        }
        return initialHeight != webDriver.findElement(cssSelector("body")).getSize().getHeight();
    }

    private boolean isMediumOrLongContentLengthSuitable(WebDriver webDriver, List<ContentLength> locationContentLength)
            throws InterruptedException {
        return locationContentLength.contains(ContentLength.fromMinutes(sumDurations(webDriver)));
    }

    private int sumDurations(WebDriver webDriver) throws InterruptedException {
//        List<WebDriver> timeSpans = webDriver.findElements(cssSelector("div.title > span"));
//        timeSpans.get(timeSpans.size() - 1).scrollTo();
        scrollMore(webDriver, 1);
        int result = 0;
        for (WebElement webElement : webDriver.findElements(cssSelector("div.title > span"))) {
            result += toMinutes(webElement.getText());
        }
        return result;
    }

    private int toMinutes(String duration) {
        int result = 1;
        String[] parts = trim(duration).split(":");
        int multiplier = 1;
        for (int i = parts.length - 2; i >= 0; i--) {
            result += Integer.parseInt(parts[i]) * multiplier;
            multiplier *= 60;
        }
        return result;
    }
}