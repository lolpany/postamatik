package lol.lolpany.postamatik;

public class YoutubeTimelineReaderFactory implements LocationTimelineReaderFactory {
    private final String chromeDriverLocation;

    public YoutubeTimelineReaderFactory(String chromeDriverLocation) {
        this.chromeDriverLocation = chromeDriverLocation;
    }

    @Override
    public LocationTimelineReader create() {
        return new YoutubeTimelineReader(chromeDriverLocation);
    }
}
