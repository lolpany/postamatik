package lol.lolpany.postamatik.bandcamp;

import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.SelenideElement;
import lol.lolpany.Account;
import lol.lolpany.Location;
import lol.lolpany.postamatik.*;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.util.*;

import static com.codeborne.selenide.Selenide.$$;
import static com.codeborne.selenide.Selenide.close;
import static com.codeborne.selenide.Selenide.open;
import static com.codeborne.selenide.WebDriverRunner.setWebDriver;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static lol.lolpany.postamatik.Postamatik.HEADLESS;
import static org.apache.commons.lang3.StringUtils.trim;

public class BandcampContentSearch implements ContentSearch {

    private final String url;
    private final Set<String> tags;

    BandcampContentSearch(String url, Set<String> tags) {
        this.url = url;
        this.tags = tags;
    }

    @Override
    public Content findContent(double precision, Set<String> tags, PostsTimeline postsTimeline, Account account, Location<LocationConfig> location) {
        Content result = null;
//        ChromeOptions chromeOptions = new ChromeOptions();
//        if (HEADLESS) {
//            chromeOptions.addArguments("headless");
//        }
//        setWebDriver(new ChromeDriver(chromeOptions));
        Configuration.baseUrl = "https://bandcamp.com";
        if (Utils.match(this.tags, tags) >= precision) {
            result = findContent(tags, postsTimeline, location);
        }
        return result;
    }

    private Content findContent(Set<String> tags, PostsTimeline postsTimeline, Location<LocationConfig> location) {
        Content result = null;
        for (int i = 1; i <= 10; i++) {
            String pageUrl = this.url + "&page=" + i ;
            open(pageUrl);

            Map<String, Pair<String, String>> albums = new HashMap<>();
            for (SelenideElement albumItem : $$("li.item")) {
                albums.put(albumItem.find("a").attr("href"),
                        new ImmutablePair<>(albumItem.find("div.itemsubtext").text(), albumItem.find("div.itemtext").text()));
            }
            for (Map.Entry<String, Pair<String, String>> album : albums.entrySet()) {
                open(album.getKey());


                if (isContentLengthSuitable(location.locationConfig.contentLength)) {
                    Content content = new Content(tags, singletonList(album.getKey()), emptyList());
                    open(pageUrl);
                    content.name = album.getValue().getLeft() + " - " + album.getValue().getRight();
                    if (!postsTimeline.isAlreadyScheduledOrUploadedOrPosted(location.url.toString(), content)) {
                        return content;
                    }
                }
            }

        }
        close();
        return result;
    }

    private boolean isContentLengthSuitable(ContentLength locationContentLength) {
        return ContentLength.fromMinutes(sumDurations()) == locationContentLength;
    }

    private int sumDurations() {
        List<SelenideElement> timeSpans = $$("div.title > span");
        timeSpans.get(timeSpans.size() - 1).scrollTo();
        return $$("div.title > span").texts().stream().map(this::toMinutes).reduce((integer, integer2) -> integer + integer2).get();
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