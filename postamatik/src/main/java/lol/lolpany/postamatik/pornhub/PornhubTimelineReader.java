package lol.lolpany.postamatik.pornhub;

import lol.lolpany.Account;
import lol.lolpany.Location;
import lol.lolpany.postamatik.LocationTimelineReader;
import lol.lolpany.postamatik.Post;

import java.util.concurrent.ConcurrentLinkedQueue;

public class PornhubTimelineReader implements LocationTimelineReader {
    @Override
    public ConcurrentLinkedQueue<Post> read(Account account, Location location) {
        return new ConcurrentLinkedQueue<>();
    }
}
