package lol.lolpany.postamatik;

import java.io.FileNotFoundException;

public class OnlinevideoconverterInputStreamFactory implements SourceInputStreamFactory {

    private final String chromeDriverLocation;
    private final String videoCache;

    public OnlinevideoconverterInputStreamFactory(String chromeDriverLocation, String videoCache) {
        this.chromeDriverLocation = chromeDriverLocation;
        this.videoCache = videoCache;
    }

    @Override
    public SourceInputStream create(String source, Content content, PostsTimeline postsTimeline, String locationUrl) throws FileNotFoundException, InterruptedException {
        return new OnlinevideoconverterInputStream(chromeDriverLocation, source, content, videoCache);
    }
}
