package lol.lolpany.postamatik;

import lol.lolpany.Account;
import lol.lolpany.Location;

import java.util.concurrent.ConcurrentLinkedQueue;

public interface LocationTimelineReader<T extends Location> {
    ConcurrentLinkedQueue<Post> read(Account account, T location);
}
