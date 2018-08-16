package lol.lolpany.postamatik.bandcamp;

import lol.lolpany.postamatik.Content;
import lol.lolpany.postamatik.PostsTimeline;
import lol.lolpany.postamatik.SourceInputStream;
import org.junit.Test;
import org.zeroturnaround.exec.ProcessExecutor;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;
import java.util.UUID;

import static java.util.Collections.singletonList;
import static lol.lolpany.postamatik.ContentStreamerDispatcher.VIDEO_CACHE;
import static lol.lolpany.postamatik.Postamatik.POSTAMATIK_HOME;

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
