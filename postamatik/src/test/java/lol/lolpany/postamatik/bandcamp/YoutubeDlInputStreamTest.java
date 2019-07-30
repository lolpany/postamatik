package lol.lolpany.postamatik.bandcamp;

import lol.lolpany.postamatik.Content;
import lol.lolpany.postamatik.PostsTimeline;
import org.junit.Test;

import static java.util.Collections.singletonList;
import static lol.lolpany.postamatik.Postamatik.VIDEO_CACHE;

public class YoutubeDlInputStreamTest {

    @Test
    public void test() throws Exception {
        new YoutubeDlAudioAndThumbInputStream(
                "https://trevorsomething.bandcamp.com/track/confessions-of-an-addict",
                new Content(null,
                        singletonList("https://trevorsomething.bandcamp.com/track/confessions-of-an-addict"),
                        null), VIDEO_CACHE, new PostsTimeline(), "").read();
    }
}
