package lol.lolpany.postamatik;

import com.codeborne.selenide.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import static com.codeborne.selenide.Selenide.*;
import static com.codeborne.selenide.WebDriverRunner.closeWebDriver;
import static com.codeborne.selenide.WebDriverRunner.setWebDriver;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static lol.lolpany.postamatik.ContentStreamerDispatcher.CHROME_DRIVER_LOCATION;
import static lol.lolpany.postamatik.SelenideUtils.getText;

class YoutubeContentSearch implements ContentSearch {

    private static final Set<String> LIVE_TEXTS = new HashSet<String>() {{
       add("LIVE NOW");
       add("СЕЙЧАС В ПРЯМОМ ЭФИРЕ");
    }};

    private final String url;
    private final Set<String> tags;

    YoutubeContentSearch(String url, Set<String> tags) {
        this.url = url;
        this.tags = tags;
        Configuration.timeout = 20000;
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
        items.should(Condition.exist);
        ElementsCollection thumbnails = items.findAll("ytd-grid-video-renderer");
        Integer prevThumbnailsNumber = 0;
        thumbnails.shouldBe(CollectionCondition.sizeGreaterThan(0));
        while (prevThumbnailsNumber < thumbnails.size()) {
            prevThumbnailsNumber = thumbnails.size();
            for (SelenideElement thumbnail : thumbnails) {
                if (thumbnail.findAll("div#details span").stream().noneMatch(e -> LIVE_TEXTS.contains(e.text()))) {
                    String source = thumbnail.find("a#thumbnail").attr("href");
                    String name = getText(thumbnail.find("a#video-title"));
                    Content content =new Content(tags, singletonList(source), emptyList());
                    content.name = name;
                    if (!postsTimeline.isAlreadyScheduledOrUploadedOrPosted(location.url.toString(), content)) {
                        return content;
                    }
                }
            }
            thumbnails.get(prevThumbnailsNumber - 1).scrollTo();
            thumbnails = $$("a#thumbnail");
        }
        return null;
    }
}
