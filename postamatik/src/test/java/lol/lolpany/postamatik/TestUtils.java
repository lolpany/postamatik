package lol.lolpany.postamatik;

import lol.lolpany.Account;
import lol.lolpany.Location;
import lol.lolpany.postamatik.pornhub.PornhubLocation;
import lol.lolpany.postamatik.youtube.YoutubeLocation;
import org.apache.commons.lang3.builder.ToStringExclude;

import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static java.util.Collections.singleton;
import static java.util.Collections.singletonList;

public class TestUtils {
    public static final String ACCOUNTS_CONFIG = "C:\\all\\info\\buffer\\postamatik-test\\accounts-config\\accounts-config.json";
    public static final String POSTS_TIMELINE = "C:\\all\\info\\buffer\\postamatik-test\\posts-timeline\\posts-timeline.json";
    public static YoutubeLocation testYoutubeLocation = null;
    public static PornhubLocation testPornhubLocation = null;

    static {
        try {
            testYoutubeLocation = new YoutubeLocation(
                    new URL("https://www.youtube.com/channel/UCa7JW9WDzT-WMolvdr5SUsg"),
                    new LocationConfig("https://www.youtube.com/channel/UCa7JW9WDzT-WMolvdr5SUsg",
                            singleton("test"), 1, 0.01, 3,
                            singletonList(ContentLength.SHORT)), "electromusic");
            testPornhubLocation = new PornhubLocation(
                    new URL("https://www.pornhub.com/users/girlcentral"),
                    new LocationConfig("https://www.pornhub.com/users/girlcentral",
                            singleton("test"), 1, 0.01, 3,
                            singletonList(ContentLength.SHORT)));
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    public static final Account TEST_ACCOUNT = new Account("funnymeatworld@gmail.com",
            "funnymeatworld@gmail.com", "asdf3f2f23@#F#@",
            Collections.singletonList(testYoutubeLocation));

    public static final Account TEST_PORNHUB_ACCOUNT = new Account("asdfasdfds222@gmail.com",
            "asdfasdfds222@gmail.com", "swdf@F#@F#@f23f",
            Collections.singletonList(testYoutubeLocation));
}
