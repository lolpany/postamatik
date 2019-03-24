package lol.lolpany.postamatik;

import java.util.List;
import java.util.Set;

public class LocationConfig {
    public String url;
    public Set<String> tags;
    public double precision;
    public double frequency;
    public Double daysPassedLimit;
    public List<ContentLength> contentLengths;

    public LocationConfig(String url, Set<String> tags, double precision, double frequency,
                          double daysPassedLimit, List<ContentLength> contentLengths) {
        this.url = url;
        this.tags = tags;
        this.precision = precision;
        this.frequency = frequency;
        this.daysPassedLimit = daysPassedLimit;
        this.contentLengths = contentLengths;
    }
}
