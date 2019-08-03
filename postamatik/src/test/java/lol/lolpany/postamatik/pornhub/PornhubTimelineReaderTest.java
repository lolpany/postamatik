package lol.lolpany.postamatik.pornhub;

import org.junit.Test;

import static lol.lolpany.postamatik.TestUtils.TEST_PORNHUB_ACCOUNT;
import static lol.lolpany.postamatik.TestUtils.testPornhubLocation;

public class PornhubTimelineReaderTest {

    @Test
    public void go() {
        System.out.println(new PornhubTimelineReader().read(TEST_PORNHUB_ACCOUNT,
                testPornhubLocation).peek().content.name);

    }
}
