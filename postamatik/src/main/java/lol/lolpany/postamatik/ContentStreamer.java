package lol.lolpany.postamatik;

import lol.lolpany.ComponentConnection;
import org.apache.commons.io.FileUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.util.concurrent.Semaphore;

import static java.nio.file.Files.deleteIfExists;
import static lol.lolpany.postamatik.ContentStreamerDispatcher.VIDEO_CACHE_PATH;

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
            post.content = sourceInputStream.read();
            if (post.content != null && !postsTimeline.isAlreadyUploadedOrPosted(locationUrl, post.content)) {
                post.setAction(locationOutputStream.write(post.content));
                posterQueue.put(post);
            } else {
                post.setPosted();
                streamerErrorQueue.offer(post);
            }
            postsTimeline.setUploaded(post);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            streamsLimitingSemaphore.release();
            if (post.content != null) {
                try {
                    if (post.content.file != null && !Files.isSameFile(post.content.file.toPath(), VIDEO_CACHE_PATH)) {
                        try {
                            FileUtils.deleteDirectory(post.content.file.getParentFile());
                        } catch (IOException e) {
                            // ignore
                        }
                    } else if (post.content.file != null) {
                        try {
                            deleteIfExists(post.content.file.toPath());
                        } catch (IOException e) {

                        }
                    }
                } catch (IOException e) {

                }
            }
        }
    }
}
