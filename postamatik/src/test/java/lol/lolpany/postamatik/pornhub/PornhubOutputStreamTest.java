package lol.lolpany.postamatik.pornhub;

import lol.lolpany.Account;
import lol.lolpany.postamatik.Content;
import lol.lolpany.postamatik.ContentLength;
import lol.lolpany.postamatik.LocationConfig;
import lol.lolpany.postamatik.youtube.YoutubeLocation;
import lol.lolpany.postamatik.youtube.YoutubeOutputStream;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.security.GeneralSecurityException;

import static java.util.Collections.singletonList;

public class PornhubOutputStreamTest {

    @Test
    public void go() throws IOException, GeneralSecurityException {
        PornhubLocation location = new PornhubLocation(new URL("https://www.pornhub.com/users/girlcentral"),
                new LocationConfig(null, null, 0, 0, 3, singletonList(ContentLength.SHORT)));
        Content content = new Content(null, null, null);
        content.name = "Cool porn";
        content.file = new File("Z:\\Blake Mitchell Wild Cherries sc 6.avi");
        new PornhubOutputStream(
                new Account("", "asdfasdfds222@gmail.com", "swdf@F#@F#@f23f",
                        singletonList(location)), location
        ).write(content);
    }
}
