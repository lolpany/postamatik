package lol.lolpany.postamatik.bandcamp;

import lol.lolpany.postamatik.Content;
import lol.lolpany.postamatik.PostsTimeline;
import lol.lolpany.postamatik.SourceInputStream;
import lol.lolpany.postamatik.SourceInputStreamFactory;

import java.io.FileNotFoundException;

public class YoutubeDlAggregateAudioInputStreamFactory implements SourceInputStreamFactory {

    private final String videoCache;

    public YoutubeDlAggregateAudioInputStreamFactory(String videoCache) {
        this.videoCache = videoCache;
    }

    @Override
    public SourceInputStream create(String source, Content content, PostsTimeline postsTimeline,
                                    String locationUrl) throws FileNotFoundException, InterruptedException {
        return new YoutubeDlAggregateAudioInputStream(source, content, videoCache, postsTimeline, locationUrl);
    }
}
