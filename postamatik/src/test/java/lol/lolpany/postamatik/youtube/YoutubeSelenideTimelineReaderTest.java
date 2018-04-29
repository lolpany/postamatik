package lol.lolpany.postamatik.youtube;

import lol.lolpany.postamatik.Account;
import lol.lolpany.postamatik.ContentLength;
import lol.lolpany.postamatik.LocationConfig;
import lol.lolpany.postamatik.Post;
import lol.lolpany.postamatik.youtube.YoutubeLocation;
import lol.lolpany.postamatik.youtube.YoutubeSelenideTimelineReader;
import org.junit.Test;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.ConcurrentLinkedQueue;

import static java.util.Collections.singletonList;
import static lol.lolpany.postamatik.ContentStreamerDispatcher.CHROME_DRIVER_LOCATION;

public class YoutubeSelenideTimelineReaderTest {

    @Test
    public void go() throws MalformedURLException {
        YoutubeLocation location = new YoutubeLocation(
                new URL("https://www.youtube.com/channel/UCRFPNeA671k2FqXIsk1e1Tg"),
                new LocationConfig(null, null, 0, 0, ContentLength.SHORT), "relax music");
        ConcurrentLinkedQueue<Post> posts = new YoutubeSelenideTimelineReader(CHROME_DRIVER_LOCATION).read(
                new Account("", "", "",
                        singletonList(location)), location);
        System.out.println(posts.stream().map((post -> post.content.name)));
    }

}
