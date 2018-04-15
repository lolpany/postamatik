package lol.lolpany.postamatik;

import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentSkipListSet;

public interface LocationTimelineReader<T extends Location> {
    ConcurrentLinkedQueue<Post> read(Account account, T location);
}
