package lol.lolpany.postamatik;

import java.time.Instant;
import java.util.function.Supplier;

import static java.lang.Thread.sleep;

public class Poster implements ComponentCycle {
    private final PosterQueue posterQueue;
    private final PostsTimeline postsTimeline;

    public Poster(PosterQueue posterQueue, PostsTimeline postsTimeline) {
        this.posterQueue = posterQueue;
        this.postsTimeline = postsTimeline;
    }

    public void doCycle() throws Exception {
        Post post = posterQueue.peek();

        if (post != null && post.time != null && post.time.isBefore(Instant.now())) {
            post.action.run();
            posterQueue.poll();
            postsTimeline.setPosted(post);
        }
        sleep(30000);
    }
}

