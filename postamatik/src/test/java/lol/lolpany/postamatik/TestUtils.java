package lol.lolpany.postamatik;

import lol.lolpany.postamatik.youtube.YoutubeLocation;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;

public class TestUtils {
    public static final String ACCOUNTS_CONFIG = "D:\\storage\\info\\buffer\\postamatik-test\\accounts-config.json";
    public static final String POSTS_TIMELINE = "D:\\storage\\info\\buffer\\postamatik-test\\posts-timeline\\posts-timeline.json";
    public static YoutubeLocation testYoutubeLocation = null;

    static {
        try {
            testYoutubeLocation = new YoutubeLocation(
                            new URL("https://www.youtube.com/channel/UCC2VdQa8i5_4GiW446zhPug"),
                            new LocationConfig("https://www.youtube.com/channel/UCC2VdQa8i5_4GiW446zhPug",
                                               Collections.singleton("test"), 1, 0.01, ContentLength.SHORT), "Test Testovich");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    public static final Account TEST_ACCOUNT = new Account("testtestovich12345678@gmail.com", "testtestovich12345678@gmail.com", "A_123456",
            Collections.singletonList(testYoutubeLocation));
}
