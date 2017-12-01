package lol.lolpany.postamatik.youtube;

import lol.lolpany.postamatik.LocationTimelineReader;
import lol.lolpany.postamatik.LocationTimelineReaderFactory;

public class YoutubeTimelineReaderFactory implements LocationTimelineReaderFactory {

    @Override
    public LocationTimelineReader create() {
        return new YoutubeTimelineReader();
    }
}
