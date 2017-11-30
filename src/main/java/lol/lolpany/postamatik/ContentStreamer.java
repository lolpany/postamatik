package lol.lolpany.postamatik;

import java.time.Instant;
import java.util.concurrent.Semaphore;

import static java.nio.file.Files.deleteIfExists;

public class ContentStreamer implements Runnable {

    private final SourceInputStream sourceInputStream;
    private final LocationOutputStream locationOutputStream;
    private final PosterQueue posterQueue;
    private final ComponentConnection<Post> streamerErrorQueue;
    private final PostsTimeline postsTimeline;
    private final Semaphore streamsLimitingSemaphore;
    private final Post post;
    private final String locationUrl;

    ContentStreamer(SourceInputStream sourceInputStream, LocationOutputStream locationOutputStream,
                    PosterQueue posterQueue, ComponentConnection<Post> streamerErrorQueue,
                    PostsTimeline postsTimeline, Semaphore streamsLimitingSemaphore, Post post, String locationUrl) {
        this.sourceInputStream = sourceInputStream;
        this.locationOutputStream = locationOutputStream;
        this.posterQueue = posterQueue;
        this.streamerErrorQueue = streamerErrorQueue;
        this.postsTimeline = postsTimeline;
        this.streamsLimitingSemaphore = streamsLimitingSemaphore;
        this.post = post;
        this.locationUrl = locationUrl;
    }

    @Override
    public void run() {
        try {
            Content content = sourceInputStream.read();
            if (!postsTimeline.isAlreadyUploadedOrPosted(locationUrl, content)) {
                post.setAction(locationOutputStream.write(content));
                posterQueue.put(post);
            } else {
                post.setPosted();
                streamerErrorQueue.offer(post);
            }
            postsTimeline.setUploaded(post);
            deleteIfExists(content.file.toPath());
            streamsLimitingSemaphore.release();
        } catch (Exception e) {

        }
    }
}
