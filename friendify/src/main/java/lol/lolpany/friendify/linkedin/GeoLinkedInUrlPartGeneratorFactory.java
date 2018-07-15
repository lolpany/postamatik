package lol.lolpany.friendify.linkedin;

import java.util.HashSet;
import java.util.Set;


public class GeoLinkedInUrlPartGeneratorFactory implements LinkedInUrlPartGeneratorFactory {

    private static final Set<String> GEO_LOCATIONS = new HashSet<String>() {{
        add("us");
        add("ca");
    }};

    @Override
    public boolean canCreate(String tag) {
        return GEO_LOCATIONS.contains(tag);
    }

    @Override
    public LinkedInGeoUrlPartGenerator create(String tag) {
        return new LinkedInGeoUrlPartGenerator(tag);
    }
}
