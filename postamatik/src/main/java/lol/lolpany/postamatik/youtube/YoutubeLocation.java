package lol.lolpany.postamatik.youtube;

import lol.lolpany.postamatik.Location;
import lol.lolpany.postamatik.LocationConfig;

import java.net.URL;

public class YoutubeLocation extends Location {
    public String channelName;

    public YoutubeLocation(URL url, LocationConfig locationConfig, String channelName) {
        super(url, locationConfig);
        this.channelName = channelName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        YoutubeLocation location = (YoutubeLocation) o;

        return channelName != null ? channelName.equals(location.channelName) : location.channelName == null;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (channelName != null ? channelName.hashCode() : 0);
        return result;
    }
}
