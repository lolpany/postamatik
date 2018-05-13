package lol.lolpany.friendify.linkedin;

import lol.lolpany.Account;
import lol.lolpany.Location;
import lol.lolpany.friendify.Connector;
import lol.lolpany.friendify.ConnectorFactory;

import java.util.concurrent.atomic.AtomicBoolean;

public class LinkedInConnectorFactory implements ConnectorFactory {
    @Override
    public Connector create(AtomicBoolean isOn, Account account, Location location) {
        return new LinkedInConnector(isOn, account, location);
    }
}
