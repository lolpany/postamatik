package lol.lolpany.postamatik;

import lol.lolpany.Account;
import lol.lolpany.Location;

public interface LocationOutputStreamFactory<T extends Location> {
     LocationOutputStream create(Account account, T location);
}
