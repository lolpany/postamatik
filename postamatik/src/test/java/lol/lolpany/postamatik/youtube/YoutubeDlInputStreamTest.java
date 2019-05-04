package lol.lolpany.postamatik.youtube;

import lol.lolpany.postamatik.Content;
import org.junit.Test;

import static lol.lolpany.postamatik.Postamatik.VIDEO_CACHE;

public class YoutubeDlInputStreamTest {
    @Test
    public void go() throws Exception {
        Content content = new YoutubeDlInputStreamFactory(VIDEO_CACHE).
                create("https://www.youtube.com/watch?v=utuxLmZyvzA",
                        new Content(null, null, null), null, "").read();
        System.out.println(content.name);
    }
}
