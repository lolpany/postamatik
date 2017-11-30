package lol.lolpany.postamatik.youtube;

import lol.lolpany.postamatik.LocationTimelineReader;
import lol.lolpany.postamatik.LocationTimelineReaderFactory;

public class YoutubeTimelineReaderFactory implements LocationTimelineReaderFactory {
    private final String chromeDriverLocation;

    public YoutubeTimelineReaderFactory(String chromeDriverLocation) {
        this.chromeDriverLocation = chromeDriverLocation;
    }

    @Override
    public LocationTimelineReader create() {
        return new YoutubeSelenideTimelineReader(chromeDriverLocation);
    }
}
