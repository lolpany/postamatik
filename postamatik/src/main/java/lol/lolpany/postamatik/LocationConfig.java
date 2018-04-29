package lol.lolpany.postamatik;

import java.net.URL;
import java.time.Duration;
import java.util.Set;

public class LocationConfig {
    public String url;
    public Set<String> tags;
    public double precision;
    public double frequency;
    public ContentLength contentLength;

    public LocationConfig(String url, Set<String> tags, double precision, double frequency, ContentLength contentLength) {
        this.url = url;
        this.tags = tags;
        this.precision = precision;
        this.frequency = frequency;
        this.contentLength = contentLength;
    }
}
