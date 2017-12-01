package lol.lolpany.postamatik;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static java.lang.Math.round;
import static java.lang.Thread.sleep;
import static java.time.temporal.ChronoUnit.DAYS;

public class Solver implements Runnable {

    private final static Duration POST_TIME = Duration.ofHours(10);
    private final static int UPLOAD_THRESHOLD = 0;
    private final ComponentConnection<AccountsConfig> accountsConfigsQueue;
    private AtomicBoolean on;
    private final PriorityComponentConnection<Post> contentStreamerQueue;
    private final ContentRepository contentRepository;
    private final PostsTimeline postsTimeline;
    private final ComponentConnection<Post> streamerErrorQueue;

    public Solver(ComponentConnection<AccountsConfig> accountsConfigsQueue,
                  PriorityComponentConnection<Post> contentStreamerQueue, ContentRepository contentRepository,
                  PostsTimeline postsTimeline, ComponentConnection<Post> streamerErrorQueue, AtomicBoolean on) {
        this.accountsConfigsQueue = accountsConfigsQueue;
        this.contentStreamerQueue = contentStreamerQueue;
        this.contentRepository = contentRepository;
        this.postsTimeline = postsTimeline;
        this.streamerErrorQueue = streamerErrorQueue;
        this.on = on;
    }

    public void run() {

        AccountsConfig accountsConfig = null;
        while (on.get()) {
            try {
                AccountsConfig newAccountsConfig = accountsConfigsQueue.poll();
                if (newAccountsConfig != null) {
                    accountsConfig = newAccountsConfig;
                }
                if (accountsConfig != null) {
                    for (Account account : accountsConfig.accountsConfig) {
                        for (Location location : account.locations) {
                            ConcurrentLinkedQueue<Post> posts = postsTimeline.getPosts(location.url.toString());
                            for (Instant instant : generateNewPostInstants(location.locationConfig, posts,
                                    UPLOAD_THRESHOLD)) {
                                Content content = contentRepository.getContent(location.locationConfig.precision,
                                        location.locationConfig.tags, location, postsTimeline);
                                if (content != null) {
                                    Post post = new Post(instant, content, account, location);
                                    contentStreamerQueue.offer(post);
                                    postsTimeline.addPost(location.url.toString(), post);
                                } else {
                                    // todo notify content-manager
                                }
                            }
                        }
                    }
                }
                Post postWithAlreadyPostedContent = streamerErrorQueue.poll();
                while (postWithAlreadyPostedContent != null) {
                    Content content = contentRepository.getContent(
                            postWithAlreadyPostedContent.location.locationConfig.precision,
                            postWithAlreadyPostedContent.location.locationConfig.tags,
                            postWithAlreadyPostedContent.location, postsTimeline);
                    if (content != null) {
                        postWithAlreadyPostedContent.content = content;
                        contentStreamerQueue.offer(postWithAlreadyPostedContent);
                    }
                    postWithAlreadyPostedContent = streamerErrorQueue.poll();
                }
                sleep(1000);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static List<Instant> generateNewPostInstants(LocationConfig locationConfig,
                                                        ConcurrentLinkedQueue<Post> posts,
                                                        int uploadThresholdFromNow) {
        List<Instant> result = new ArrayList<>();
        Instant lastPostTime = Instant.ofEpochMilli(0);
        if (posts != null) {
//            lastPostTime = posts.get(0).time;
            for (Post post : posts) {
                if (post.time != null && lastPostTime.isBefore(post.time)) {
                    lastPostTime = post.time;
                }
            }
        } /*else {
            lastPostTime = (LocalTime.of(0, 0)).atDate(LocalDate.now().minus(1, ChronoUnit.DAYS))
                    .toInstant(OffsetDateTime.now().getOffset());
        }*/
        long period = round(1 / locationConfig.frequency * TimeUnit.DAYS.toSeconds(1));
        if (Instant.now().plus(-period, ChronoUnit.SECONDS).isAfter(lastPostTime)) {
            lastPostTime = Instant.now().truncatedTo(DAYS).plus(POST_TIME).plus(-period, ChronoUnit.SECONDS);
        }
        Instant instant = lastPostTime.plus(period, ChronoUnit.SECONDS);
        Instant uploadThreshold = Instant.now().plus(uploadThresholdFromNow, DAYS);
        while (instant.isBefore(uploadThreshold)) {
            result.add(instant);
            instant = instant.plus(period, ChronoUnit.SECONDS);
        }
        return result;
    }

}
