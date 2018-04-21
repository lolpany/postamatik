package lol.lolpany.postamatik.youtube;

import lol.lolpany.postamatik.Account;
import org.junit.Test;

import static lol.lolpany.postamatik.TestUtils.TEST_ACCOUNT;
import static lol.lolpany.postamatik.TestUtils.testYoutubeLocation;

public class YoutubeTimelineReaderTest {

    @Test
    public void go() {
        System.out.println(new YoutubeTimelineReader().read(TEST_ACCOUNT,
                testYoutubeLocation).peek().content.name);
    }
}
