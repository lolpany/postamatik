package lol.lolpany.postamatik;

import java.io.IOException;
import java.security.GeneralSecurityException;

public class YoutubeOutputStreamFactory implements LocationOutputStreamFactory<YoutubeLocation> {

    private final String chromeDriverLocation;

    YoutubeOutputStreamFactory(String chromeDriverLocation) throws GeneralSecurityException, IOException {
        this.chromeDriverLocation = chromeDriverLocation;
    }

    @Override
    public LocationOutputStream create(Account account, YoutubeLocation location) {
        return new YoutubeOutputStream(chromeDriverLocation, account, location);
    }
}
