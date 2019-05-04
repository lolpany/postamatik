package lol.lolpany.postamatik.bandcamp;

import lol.lolpany.postamatik.Content;
import org.junit.Test;

import static java.util.Collections.singletonList;
import static lol.lolpany.postamatik.Postamatik.VIDEO_CACHE;

public class YoutubeDlAggregateAudioInputStreamTest {

    @Test
    public void test() throws Exception {
        new YoutubeDlAggregateAudioInputStream(
                "https://radicaldreamland.bandcamp.com/album/celeste-original-soundtrack",
                new Content(null,
                        singletonList("https://radicaldreamland.bandcamp.com/album/celeste-original-soundtrack"),
                        null), VIDEO_CACHE, null, "").read();
    }
}
