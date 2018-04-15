package lol.lolpany.postamatik;

import java.net.URL;

public class Location {
    public URL url;
    public LocationConfig locationConfig;

    public Location(URL url, LocationConfig locationConfig) {
        this.url = url;
        this.locationConfig = locationConfig;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Location location = (Location) o;

        if (url != null ? !url.equals(location.url) : location.url != null) return false;
        return locationConfig != null ? locationConfig.equals(location.locationConfig) : location.locationConfig == null;
    }

    @Override
    public int hashCode() {
        int result = url != null ? url.hashCode() : 0;
        result = 31 * result + (locationConfig != null ? locationConfig.hashCode() : 0);
        return result;
    }
}