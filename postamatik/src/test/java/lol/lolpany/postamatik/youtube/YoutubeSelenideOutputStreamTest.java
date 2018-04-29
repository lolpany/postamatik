package lol.lolpany.postamatik.youtube;

import lol.lolpany.postamatik.Account;
import lol.lolpany.postamatik.Content;
import lol.lolpany.postamatik.ContentLength;
import lol.lolpany.postamatik.LocationConfig;
import lol.lolpany.postamatik.youtube.YoutubeLocation;
import lol.lolpany.postamatik.youtube.YoutubeSelenideOutputStream;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import static java.util.Collections.singletonList;
import static lol.lolpany.postamatik.ContentStreamerDispatcher.CHROME_DRIVER_LOCATION;

public class YoutubeSelenideOutputStreamTest {

    @Test
    public void go() throws IOException {
        YoutubeLocation location = new YoutubeLocation(new URL("https://www.youtube.com/channel/UCcpxdjh8t3e65jGqdiADYWA"),
                new LocationConfig(null, null, 0, 0, ContentLength.SHORT), "Sergey Golovachev");
        Content content = new Content(null, null, null);
        content.name = "Her[x]  ||  Autumn EP";
        content.file = new File("D:\\buffer\\Her[x]    Autumn EP.mp4");
        new YoutubeSelenideOutputStream(CHROME_DRIVER_LOCATION,
                new Account("", "", "",
                        singletonList(location)), location
        ).write(content).run();
    }
}
