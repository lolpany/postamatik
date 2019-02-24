package lol.lolpany.postamatik.bandcamp;

import lol.lolpany.postamatik.Content;
import lol.lolpany.postamatik.PostsTimeline;
import lol.lolpany.postamatik.SourceInputStream;
import lol.lolpany.postamatik.SourceInputStreamFactory;

import java.io.FileNotFoundException;

public class YoutubeDlAudioAndThumbInputStreamFactory implements SourceInputStreamFactory {

    private final String videoCache;

    public YoutubeDlAudioAndThumbInputStreamFactory(String videoCache) {
        this.videoCache = videoCache;
    }

    @Override
    public SourceInputStream create(String source, Content content, PostsTimeline postsTimeline,
                                    String locationUrl) throws FileNotFoundException, InterruptedException {
        return new YoutubeDlAudioAndThumbInputStream(source, content, videoCache, postsTimeline, locationUrl);
    }
}
