package lol.lolpany.friendify;

import lol.lolpany.Account;
import lol.lolpany.Location;

import java.util.concurrent.atomic.AtomicBoolean;

public interface ConnectorFactory<T extends Location<LocationConfig>> {
    Connector create(AtomicBoolean isOn, Account<LocationConfig> account, T location);
}
