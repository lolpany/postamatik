package lol.lolpany.postamatik.bandcamp;

import lol.lolpany.postamatik.LocationTimelineReader;
import lol.lolpany.postamatik.LocationTimelineReaderFactory;

public class BandcampTimelineReaderFactory implements LocationTimelineReaderFactory {
    @Override
    public LocationTimelineReader create() {
        return new BandcampTimelineReader();
    }
}

