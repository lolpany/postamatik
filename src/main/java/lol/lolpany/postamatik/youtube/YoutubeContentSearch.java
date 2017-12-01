package lol.lolpany.postamatik.youtube;

import com.codeborne.selenide.*;
import lol.lolpany.postamatik.*;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import static com.codeborne.selenide.CollectionCondition.sizeGreaterThan;
import static com.codeborne.selenide.Selenide.*;
import static com.codeborne.selenide.WebDriverRunner.closeWebDriver;
import static com.codeborne.selenide.WebDriverRunner.setWebDriver;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static lol.lolpany.postamatik.ContentStreamerDispatcher.CHROME_DRIVER_LOCATION;
import static lol.lolpany.postamatik.SelenideUtils.getText;

public class YoutubeContentSearch implements ContentSearch {

    private static final Set<String> LIVE_TEXTS = new HashSet<String>() {{
        add("LIVE NOW");
        add("СЕЙЧАС В ПРЯМОМ ЭФИРЕ");
    }};

    private final String url;
    private final Set<String> tags;

    YoutubeContentSearch(String url, Set<String> tags) {
        this.url = url;
        this.tags = tags;
    }

    public Content findContent(double precision, Set<String> tags, PostsTimeline postsTimeline, Location location)
            throws MalformedURLException {
        Content result = null;
        if (Utils.match(this.tags, tags) >= precision) {
            System.setProperty("webdriver.chrome.driver", CHROME_DRIVER_LOCATION);
            ChromeOptions chromeOpts = new ChromeOptions();
            chromeOpts.addArguments("headless");
            setWebDriver(new ChromeDriver(chromeOpts));
//        authorize(account, location);
            open(url + "?sort=p");
            if (new URL(url).getFile().endsWith("/videos")) {
                result = findContentOnChannel(url, tags, postsTimeline, location);
            }
            close();
            closeWebDriver();
        }
        return result;
    }

    private Content findContentOnChannel(String url, Set<String> tags, PostsTimeline postsTimeline,
                                         Location location) {
        open(url + "?view=0");
        SelenideElement items = $("div#items");
        boolean commonLayout = false;
        try {
            items.should(Condition.exist);
            commonLayout = true;
        } catch (Throwable e) {

        }
        if (commonLayout) {
            ElementsCollection thumbnails = items.findAll(commonLayout ? "ytd-grid-video-renderer"
                    : "ul#channels-browse-content-grid div.yt-lockup-dismissable");
            Integer prevThumbnailsNumber = 0;
            thumbnails.shouldBe(sizeGreaterThan(0));
            while (prevThumbnailsNumber < thumbnails.size()) {
                prevThumbnailsNumber = thumbnails.size();
                for (SelenideElement thumbnail : thumbnails) {
                    if (thumbnail.findAll(commonLayout ? "div#details span" : "div.yt-lockup-content").stream()
                            .noneMatch(e -> LIVE_TEXTS.contains(e.text()))) {
                        String source = thumbnail.find(commonLayout ? "a#thumbnail" : "a.yt-uix-sessionlink").attr("href");

                        String name = getText(thumbnail.find(commonLayout ? "a#video-title" : "div.yt-lockup-content a"));

                        Content content = new Content(tags, singletonList(source), emptyList());
                        content.name = name;
                        if (!postsTimeline.isAlreadyScheduledOrUploadedOrPosted(location.url.toString(), content)) {
                            return content;
                        }
                    }
                }
                thumbnails.get(prevThumbnailsNumber - 1).scrollTo();
                thumbnails = $$("a#thumbnail");
            }
        }
        return null;
    }
}
