package lol.lolpany.postamatik;

import lol.lolpany.Account;
import lol.lolpany.Location;

public interface PostActionFactory<T extends PostAction, P extends Location> {

    T create(Account account, P location, String id);
}
