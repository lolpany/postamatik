package lol.lolpany.postamatik.youtube;

import lol.lolpany.postamatik.Account;
import lol.lolpany.postamatik.LocationConfig;
import lol.lolpany.postamatik.Post;
import lol.lolpany.postamatik.PosterQueue;
import lol.lolpany.postamatik.youtube.YoutubeLocation;
import lol.lolpany.postamatik.youtube.YoutubeSelenidePostAction;
import org.junit.Test;

import java.net.URL;
import java.time.Instant;

import static java.util.Collections.singletonList;
import static org.apache.commons.io.FileUtils.writeStringToFile;

public class YoutubeSelenidePostActionTest {
    @Test
    public void persist() throws Exception {
        PosterQueue posterQueue = new PosterQueue(10);
        YoutubeLocation location = new YoutubeLocation(new URL("https://www.youtube.com/channel/UCcpxdjh8t3e65jGqdiADYWA"),
                new LocationConfig(null, null, 0, 0), "Electronic Music");
        Account account = new Account("", "", "",
                singletonList(location));
        Post post = new Post(Instant.now(), null, account, location);
        post.setAction(new YoutubeSelenidePostAction("", account, location, "lol"));
        posterQueue.put(post);
    }
}