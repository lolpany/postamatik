package lol.lolpany.postamatik.youtube;

import lol.lolpany.Account;
import lol.lolpany.postamatik.LocationOutputStream;
import lol.lolpany.postamatik.LocationOutputStreamFactory;

import java.io.IOException;
import java.security.GeneralSecurityException;

public class YoutubeOutputStreamFactory implements LocationOutputStreamFactory<YoutubeLocation> {

    private final String chromeDriverLocation;

    public YoutubeOutputStreamFactory(String chromeDriverLocation) throws GeneralSecurityException, IOException {
        this.chromeDriverLocation = chromeDriverLocation;
    }

    @Override
    public LocationOutputStream create(Account account, YoutubeLocation location) {
        return new YoutubeOutputStream(account, location);
    }
}
