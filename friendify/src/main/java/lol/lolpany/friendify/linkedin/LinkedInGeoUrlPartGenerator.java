package lol.lolpany.friendify.linkedin;

import java.net.URLEncoder;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class LinkedInGeoUrlPartGenerator implements ResettableIterator<String> {

    private static final Map<String, int[]> LOCATIONS = Collections.unmodifiableMap(new HashMap<String, int[]>() {{
        put("us", new int[] {
                0 // all
                , 7 // Greater Boston Area
                , 14 // Greater Chicago Area
                , 49 // Greater Los Angeles Area
                , 51 // Orange County, California Area
                , 70 // Greater New York City Area
        });
    }});

    private final String country;
    private int i = 0;

    public LinkedInGeoUrlPartGenerator(String country) {
        this.country = country;
    }

    @Override
    public boolean hasNext() {
        return i < LOCATIONS.get(country).length;
    }

    @Override
    public String next() {
        String result = "facetGeoRegion=" + URLEncoder.encode("[\"" + country + ":" + LOCATIONS.get(country)[i] + "\"]");
        i++;
        return result;
    }

    @Override
    public void reset() {
        this.i = 0;
    }
}
