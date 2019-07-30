package lol.lolpany.postamatik.pornhub;

import lol.lolpany.postamatik.LocationTimelineReader;
import lol.lolpany.postamatik.LocationTimelineReaderFactory;

public class PornhubTimelineReaderFactory implements LocationTimelineReaderFactory {
    @Override
    public LocationTimelineReader create() {
        return new PornhubTimelineReader();
    }
}

