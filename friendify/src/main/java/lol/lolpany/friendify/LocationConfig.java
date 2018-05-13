package lol.lolpany.friendify;

import java.util.Set;

public class LocationConfig {
//    public String url;
    public Set<String> tags;
    public double frequency;

    public LocationConfig(Set<String> tags, double frequency) {
        this.tags = tags;
        this.frequency = frequency;
    }
}
