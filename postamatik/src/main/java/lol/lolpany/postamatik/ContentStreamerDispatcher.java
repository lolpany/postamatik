package lol.lolpany.postamatik;

import lol.lolpany.ComponentConnection;
import lol.lolpany.postamatik.bandcamp.YoutubeDlAggregateAudioInputStreamFactory;
import lol.lolpany.postamatik.bandcamp.YoutubeDlAudioAndThumbInputStreamFactory;
import lol.lolpany.postamatik.youtube.YoutubeDlInputStreamFactory;
import lol.lolpany.postamatik.youtube.YoutubeOutputStreamFactory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiPredicate;

import static java.lang.Thread.sleep;

public class ContentStreamerDispatcher implements Runnable {

    public final static Path VIDEO_CACHE_PATH = Paths.get(Postamatik.VIDEO_CACHE);
    private final static int STREAMER_BUFFER_SIZE = 10240;
    private final static int CORE_POOL_SIZE = 10240;
    private final static int MAXIMUM_POOL_SIZE = 10240;
    private final static Map<BiPredicate<String, ContentLength>, SourceInputStreamFactory> SOURCE_INPUT_STREAM_FACTORIES =
            new HashMap<>() {{
                put((url, contentLength) -> url.contains("www.youtube.com"), new YoutubeDlInputStreamFactory(Postamatik.VIDEO_CACHE));
                put((url, contentLength) -> url.contains("/album/") && contentLength == ContentLength.LONG, new YoutubeDlAggregateAudioInputStreamFactory(Postamatik.VIDEO_CACHE));
                put((url, contentLength) -> url.contains("bandcamp.com") && contentLength == ContentLength.SHORT, new YoutubeDlAudioAndThumbInputStreamFactory(Postamatik.VIDEO_CACHE));
            }};
    private final static Map<String, LocationOutputStreamFactory> LOCATION_OUTPUT_STREAM_FACTORIES =
            new HashMap<>() {{
                try {
                    put("www.youtube.com", new YoutubeOutputStreamFactory(Postamatik.CHROME_DRIVER_LOCATION));
                } catch (GeneralSecurityException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }};

    private final AtomicBoolean isOn;
    private final PriorityComponentConnection<Post> contentStreamerQueue;
    private final Semaphore streamsLimitingSemaphore;
    private final PosterQueue posterQueue;
    private final ComponentConnection<Post> streamerErrorQueue;
    private final ExecutorService executorService;
    private final PostsTimeline postsTimeline;


    public ContentStreamerDispatcher(AtomicBoolean isOn, PriorityComponentConnection<Post> contentStreamerQueue,
                                     PosterQueue posterQueue,
                                     ComponentConnection<Post> streamerErrorQueue,
                                     PostsTimeline postsTimeline) {
        this.isOn = isOn;
        this.contentStreamerQueue = contentStreamerQueue;
        this.streamsLimitingSemaphore = new Semaphore(Postamatik.CONTENT_STREAMER_MAXIMUM_PARALLER_STREAMS);
        this.posterQueue = posterQueue;
        this.streamerErrorQueue = streamerErrorQueue;
        this.postsTimeline = postsTimeline;
        this.executorService = new ThreadPoolExecutor(CORE_POOL_SIZE, MAXIMUM_POOL_SIZE, 5, TimeUnit.MINUTES,
                new ArrayBlockingQueue<>(20));
    }

    @Override
    public void run() {
        try {
            while (isOn.get()) {
                Post post = contentStreamerQueue.poll();
                if (post != null) {
                    boolean submitted = false;
                    while (!submitted) {
                        try {
                            try {
                                while (isOn.get() && !streamsLimitingSemaphore.tryAcquire()) {
                                    sleep(1000);
                                }
                                executorService.submit(new ContentStreamer(identifySourceInputStream(post,
                                        post.location.url.toString()),
                                        identifyLocationOutputStream(post), posterQueue, streamerErrorQueue,
                                        postsTimeline, streamsLimitingSemaphore, post, post.location.url.toString()));

                            } catch (MalformedURLException | FileNotFoundException e) {
                                e.printStackTrace();
                            }
                            submitted = true;
                        } catch (RejectedExecutionException e) {
                            // ignore
                        }
                    }
                }
                sleep(1000);
            }
            executorService.shutdown();
        } catch (InterruptedException e) {
            // already out of while, ignore
        }
    }

    private SourceInputStream identifySourceInputStream(Post post, String locationUrl) throws MalformedURLException, FileNotFoundException, InterruptedException {
        SourceInputStream result = null;
        ContentLength contentLength = post.location.locationConfig.contentLengths.get(new Random().nextInt(post.location.locationConfig.contentLengths.size()));
        for (Map.Entry<BiPredicate<String, ContentLength>, SourceInputStreamFactory> factories : SOURCE_INPUT_STREAM_FACTORIES.entrySet()) {
            if (factories.getKey().test(post.content.getActualSource(), contentLength)) {
                result = factories.getValue().create(post.content.getActualSource(), post.content, postsTimeline, locationUrl);
                break;
            }
        }
        return result;
    }

    private LocationOutputStream identifyLocationOutputStream(Post post) {
        return LOCATION_OUTPUT_STREAM_FACTORIES.get(post.location.url.getHost()).create(post.account, post.location);
    }
}
