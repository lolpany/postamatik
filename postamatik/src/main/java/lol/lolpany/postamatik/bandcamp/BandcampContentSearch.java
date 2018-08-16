package lol.lolpany.postamatik.bandcamp;

import com.codeborne.selenide.SelenideElement;
import lol.lolpany.Account;
import lol.lolpany.Location;
import lol.lolpany.postamatik.*;
import org.openqa.selenium.Keys;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static com.codeborne.selenide.Selenide.*;
import static com.codeborne.selenide.WebDriverRunner.getWebDriver;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.apache.commons.lang3.StringUtils.trim;

public class BandcampContentSearch implements ContentSearch {

    private final Set<String> tags;

    BandcampContentSearch(Set<String> tags) {
        this.tags = tags;
    }

    @Override
    public Content findContent(double precision, Set<String> tags, PostsTimeline postsTimeline, Account account, Location<LocationConfig> location) {
        Content result = null;
        if (Utils.match(this.tags, tags) >= precision) {
            result = findContent(tags, postsTimeline, location);
        }
        return result;
    }

    private Content findContent(Set<String> tags, PostsTimeline postsTimeline, Location<LocationConfig> location) {
        for (int i = 1; i <= 10; i++) {
            String pageUrl = "https://bandcamp.com/tag/" + tags.iterator().next() + "?page=" + i + "&sort_field=pop";
            open(pageUrl);
            for (int j = 0; j < 40; j++) {

                SelenideElement albumItem = $$("li.item").get(j);
                albumItem.scrollTo();


                String albumUrl = albumItem.find("a").attr("href");
                open(albumUrl);


                if (isContentLengthSuitable(location.locationConfig.contentLength)) {
                    Content content = new Content(tags, singletonList(albumUrl), emptyList());
                    open(pageUrl);
                    content.name = albumItem.find("div.itemsubtext").text() + "-" + albumItem.find("div.itemtext").text();
                    if (!postsTimeline.isAlreadyScheduledOrUploadedOrPosted(location.url.toString(), content)) {
                        return content;
                    }
                }

            }
        }
        return null;
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