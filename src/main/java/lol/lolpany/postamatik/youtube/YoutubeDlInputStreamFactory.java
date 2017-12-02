package lol.lolpany.postamatik.youtube;

import lol.lolpany.postamatik.Content;
import lol.lolpany.postamatik.PostsTimeline;
import lol.lolpany.postamatik.SourceInputStream;
import lol.lolpany.postamatik.SourceInputStreamFactory;

import java.io.FileNotFoundException;

public class YoutubeDlInputStreamFactory implements SourceInputStreamFactory {

    private final String videoCache;

    public YoutubeDlInputStreamFactory(String videoCache) {
        this.videoCache = videoCache;
    }

    @Override
    public SourceInputStream create(String source, Content content, PostsTimeline postsTimeline,
                                    String locationUrl) throws FileNotFoundException, InterruptedException {
        return new YoutubeDlInputStream(source, content, videoCache, postsTimeline, locationUrl);
    }
}
