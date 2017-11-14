package lol.lolpany.postamatik;

import org.junit.Test;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.ConcurrentLinkedQueue;

import static java.util.Collections.singletonList;
import static lol.lolpany.postamatik.ContentStreamerDispatcher.CHROME_DRIVER_LOCATION;

public class YoutubeTimelineReaderTest {

    @Test
    public void go() throws MalformedURLException {
        YoutubeLocation location = new YoutubeLocation(
                new URL("https://www.youtube.com/channel/UCRFPNeA671k2FqXIsk1e1Tg"),
                new LocationConfig(null, null, 0, 0), "relax music");
        ConcurrentLinkedQueue<Post> posts = new YoutubeTimelineReader(CHROME_DRIVER_LOCATION).read(
                new Account("", "", "",
                        singletonList(location)), location);
        System.out.println(posts.stream().map((post -> post.content.name)));
    }

}
