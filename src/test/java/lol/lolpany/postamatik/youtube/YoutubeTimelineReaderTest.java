package lol.lolpany.postamatik.youtube;

import lol.lolpany.postamatik.Account;
import org.junit.Test;

public class YoutubeTimelineReaderTest {

    @Test
    public void go() {
        System.out.println(new YoutubeTimelineReader().read(new Account("funnymeatworld@gmail.com",
                "funnymeatworld@gmail.com",
                "As123456", null), new YoutubeLocation(null, null, "supergame")).peek().content.name);
    }
}
