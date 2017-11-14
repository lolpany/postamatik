package lol.lolpany.postamatik;

import com.google.gson.Gson;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.net.MalformedURLException;
import java.time.Duration;
import java.time.Instant;
import java.time.Period;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import static java.lang.Math.round;
import static java.lang.Thread.sleep;

public class Solver implements Runnable {

    private final static Duration POST_TIME = Duration.ofHours(10);
    private final static Period UPLOAD_THRESHOLD = Period.ofDays(5);
    private final ComponentConnection<AccountsConfig> accountsConfigsQueue;
    private AtomicBoolean on;
    private final PriorityComponentConnection<Post> contentStreamerQueue;
    private final ContentRepository contentRepository;
    private final PostsTimeline postsTimeline;

    public Solver(ComponentConnection<AccountsConfig> accountsConfigsQueue,
                  PriorityComponentConnection<Post> contentStreamerQueue, ContentRepository contentRepository,
                  PostsTimeline postsTimeline, AtomicBoolean on) {
        this.accountsConfigsQueue = accountsConfigsQueue;
        this.contentStreamerQueue = contentStreamerQueue;
        this.contentRepository = contentRepository;
        this.postsTimeline = postsTimeline;
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
                sleep(1000);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static List<Instant> generateNewPostInstants(LocationConfig locationConfig,
                                                        ConcurrentLinkedQueue<Post> posts,
                                                        Period uploadThresholdFromNow) {
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
            lastPostTime = Instant.now().truncatedTo(ChronoUnit.DAYS).plus(POST_TIME).plus(-period, ChronoUnit.SECONDS);
        }
        Instant instant = lastPostTime.plus(period, ChronoUnit.SECONDS);
        Instant uploadThreshold = Instant.now().plus(uploadThresholdFromNow);
        while (instant.isBefore(uploadThreshold)) {
            result.add(instant);
            instant = instant.plus(period, ChronoUnit.SECONDS);
        }
        return result;
    }

}
