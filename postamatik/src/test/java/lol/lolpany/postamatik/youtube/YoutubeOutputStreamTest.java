package lol.lolpany.postamatik.youtube;

import lol.lolpany.postamatik.Account;
import lol.lolpany.postamatik.Content;
import lol.lolpany.postamatik.ContentLength;
import lol.lolpany.postamatik.LocationConfig;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.security.GeneralSecurityException;

import static java.util.Collections.singletonList;

public class YoutubeOutputStreamTest {

    @Test
    public void go() throws IOException, GeneralSecurityException {
        YoutubeLocation location = new YoutubeLocation(new URL("https://www.youtube.com/channel/UCcpxdjh8t3e65jGqdiADYWA"),
                new LocationConfig(null, null, 0, 0, ContentLength.SHORT), "supergame");
        Content content = new Content(null, null, null);
        content.name = "Her[x]  ||  Autumn EP";
        content.file = new File("D:\\buffer\\lol.mp4");
        new YoutubeOutputStream(
                new Account("", "funnymeatworld@gmail.com", "As123456",
                        singletonList(location)), location
        ).write(content).run();
    }
}
