package lol.lolpany.postamatik;

import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import static java.util.Collections.singletonList;
import static lol.lolpany.postamatik.ContentStreamerDispatcher.CHROME_DRIVER_LOCATION;

public class YoutubeOutputStreamTest {

    @Test
    public void go() throws IOException {
        YoutubeLocation location = new YoutubeLocation(new URL("https://www.youtube.com/channel/UCcpxdjh8t3e65jGqdiADYWA"),
                new LocationConfig(null, null, 0, 0), "Sergey Golovachev");
        Content content = new Content(null, null, null);
        content.name = "Her[x]  ||  Autumn EP";
        content.file = new File("D:\\buffer\\Her[x]    Autumn EP.mp4");
        new YoutubeOutputStream(CHROME_DRIVER_LOCATION,
                new Account("", "", "",
                        singletonList(location)), location
        ).write(content).run();
    }
}
