package lol.lolpany.postamatik;

import java.util.ArrayList;
import java.util.List;

public class PersistedPosts {
    List<Post> posts;

    public PersistedPosts() {
    }

    public PersistedPosts(PosterQueue posterQueue) {
        posts = new ArrayList<>(posterQueue.queue.queue);
    }
}
