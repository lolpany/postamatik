package lol.lolpany.postamatik.pornhub;

import lol.lolpany.Account;
import lol.lolpany.postamatik.LocationOutputStream;
import lol.lolpany.postamatik.LocationOutputStreamFactory;
import lol.lolpany.postamatik.youtube.YoutubeLocation;

import java.io.IOException;
import java.security.GeneralSecurityException;

public class PornhubOutputStreamFactory implements LocationOutputStreamFactory<PornhubLocation> {

    private final String chromeDriverLocation;

    public PornhubOutputStreamFactory(String chromeDriverLocation) throws GeneralSecurityException, IOException {
        this.chromeDriverLocation = chromeDriverLocation;
    }

    @Override
    public LocationOutputStream create(Account account, PornhubLocation location) {
        return new PornhubOutputStream(account, location);
    }
}
