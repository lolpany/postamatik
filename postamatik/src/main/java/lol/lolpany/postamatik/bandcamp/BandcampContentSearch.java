package lol.lolpany.postamatik.bandcamp;

import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.SelenideElement;
import lol.lolpany.Account;
import lol.lolpany.Location;
import lol.lolpany.postamatik.*;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Triple;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;

import static com.codeborne.selenide.Selenide.*;
import static com.codeborne.selenide.WebDriverRunner.*;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static lol.lolpany.postamatik.Postamatik.*;
import static lol.lolpany.postamatik.SelenideUtils.isDaysPassed;
import static org.apache.commons.lang3.StringUtils.trim;

public class BandcampContentSearch implements ContentSearch {

    private final String url;
    private final Set<String> tags;

    BandcampContentSearch(String url, Set<String> tags) {
        this.url = url;
        this.tags = tags;
    }

    @Override
    public Content findContent(double precision, Set<String> tags, PostsTimeline postsTimeline, Account account,
                               Location<LocationConfig> location) {
        try {
            Content result = null;
            System.setProperty("webdriver.chrome.driver", CHROME_DRIVER_LOCATION);
            ChromeOptions chromeOptions = new ChromeOptions();
            chromeOptions.addArguments("--no-sandbox");
            if (HEADLESS) {
                chromeOptions.setHeadless(true);
            }
            setWebDriver(new ChromeDriver(chromeOptions));

//            System.setProperty("webdriver.gecko.driver", GECKO_DRIVER_LOCATION);
//            FirefoxOptions firefoxOptions = new FirefoxOptions();
//            firefoxOptions.setAcceptInsecureCerts(true);
//            if (HEADLESS) {
//                firefoxOptions.setHeadless(true);
//            }
//            setWebDriver(new FirefoxDriver(firefoxOptions));

            Configuration.baseUrl = "https://bandcamp.com";

            if (Utils.match(this.tags, tags) >= precision) {
                open(this.url);
                sleep(5000);
                if (!$("div.follow").exists()) {
                    result = findContentOld(tags, postsTimeline, location);
                } else {
                    result = findContentNew(tags, postsTimeline, location);
                }
            }
            return result;
        } finally {
            closeWebDriver();
        }

    }

    private Content findContentOld(Set<String> tags, PostsTimeline postsTimeline, Location<LocationConfig> location) {
        Content result = null;
        for (int i = 1; i <= 10; i++) {
            String pageUrl = this.url + (this.url.contains("?") ? "&" : "?") + "page=" + i;
            open(pageUrl);

            List<Triple<String, String, String>> albums = new ArrayList<>();
            for (SelenideElement albumItem : $$("li.item")) {
                albums.add(new ImmutableTriple<>(albumItem.find("a").attr("href"),
                        albumItem.find("div.itemsubtext").text(), albumItem.find("div.itemtext").text()));
            }
            for (Triple<String, String, String> album : albums) {
                result = extractContent(postsTimeline, location, pageUrl, album);
                if (result != null) {
                    return result;
                }
            }

        }
        return result;
    }


    private Content findContentNew(Set<String> tags, PostsTimeline postsTimeline, Location<LocationConfig> location) {
        String pageUrl = this.url;
        open(pageUrl);
        SelenideElement button = $("button.view-more");
        button.exists();
        button.click();
        int i = 0;
        while (scrollMore()) {
            List<Triple<String, String, String>> albums = new ArrayList<>();
            for (SelenideElement albumItem : $$("div#dig-deeper div.dig-deeper-item")) {
                albums.add(new ImmutableTriple<>(albumItem.find("a").attr("href"),
                        albumItem.find("div.artist > span").text(), albumItem.find("div.title").text()));
            }
            for (Triple<String, String, String> album : albums.subList(i, albums.size())) {
                Content result = extractContent(postsTimeline, location, pageUrl, album);
                if (result != null) {
                    return result;
                }
                i++;
            }
        }
        return null;
    }

    private Content extractContent(PostsTimeline postsTimeline, Location<LocationConfig> location, String pageUrl,
                                   Triple<String, String, String> album) {
        open(album.getLeft());
        ContentLength contentLength = location.locationConfig.contentLengths
                .get(new Random().nextInt(location.locationConfig.contentLengths.size()));
        switch (contentLength) {
            case LONG:
            case MEDIUM:
                if (isMediumOrLongContentLengthSuitable(location.locationConfig.contentLengths)) {
                    Content content = new Content(tags, singletonList(album.getLeft()), emptyList());
                    open(pageUrl);
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
                List<SelenideElement> timeSpans = $$("div.title > span");
                timeSpans.get(timeSpans.size() - 1).scrollTo();
                for (SelenideElement track : $$("div.title")) {
                    if (isShortContentLengthSuitable(location.locationConfig.contentLengths, track)) {
                        Content content = new Content(tags, singletonList(track.find("a").attr("href")), emptyList());
                        String trackLabel = track.find("a").find("span").text();
                        if (trackLabel.contains("-")) {
                            content.name = trackLabel;
                        } else {
                            content.name = $("div#name-section span > a").text() + " - " + trackLabel;
                        }
                        if (!postsTimeline.isAlreadyScheduledOrUploadedOrPosted(location.url.toString(), content)) {
                            return content;
                        }
                        content.time = parseContentTime();
                        if (isDaysPassed(location.locationConfig, content)) {
                            return null;
                        }
                    }
                }
        }
        return null;
    }

    private Instant parseContentTime() {
        String date = $("#trackInfoInner > .tralbum-credits > meta[itemprop=\"datePublished\"]").attr("content");
        return LocalDate.of(Integer.parseInt(date.substring(0, 4)), Integer.parseInt(date.substring(4, 6)),
                Integer.parseInt(date.substring(6, 8))).atStartOfDay().toInstant(
                ZoneOffset.UTC);
    }

    private boolean isShortContentLengthSuitable(List<ContentLength> locationContentLength, SelenideElement track) {
        if (track.find("span.time").exists()) {
            return locationContentLength.contains(ContentLength.fromMinutes(toMinutes(track.find("span.time").text())));
        } else {
            return false;
        }
    }

    private boolean scrollMore() {
        WebDriver webDriver = getWebDriver();
        int initialHeight = $("body").getSize().getHeight();
        int i = 0;
        while (initialHeight == $("body").getSize().getHeight() && i < 5) {
            i++;
            ((JavascriptExecutor) webDriver)
                    .executeScript("window.scrollTo(0, document.body.scrollHeight)");
            sleep(5000);
            ((JavascriptExecutor) webDriver)
                    .executeScript("window.scrollTo(0, document.body.scrollHeight)");
        }
        return initialHeight != $("body").getSize().getHeight();
    }

    private boolean isMediumOrLongContentLengthSuitable(List<ContentLength> locationContentLength) {
        return locationContentLength.contains(ContentLength.fromMinutes(sumDurations()));
    }

    private int sumDurations() {
        List<SelenideElement> timeSpans = $$("div.title > span");
        timeSpans.get(timeSpans.size() - 1).scrollTo();
        return $$("div.title > span").texts().stream().map(this::toMinutes)
                .reduce((integer, integer2) -> integer + integer2).get();
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