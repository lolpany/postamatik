package lol.lolpany.postamatik;

import lol.lolpany.Account;
import lol.lolpany.AccountsConfig;
import lol.lolpany.ComponentConnection;
import lol.lolpany.Location;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static java.lang.Math.round;
import static java.lang.Thread.sleep;
import static java.time.temporal.ChronoUnit.DAYS;

public class Solver implements Runnable {

    private final static Duration POST_TIME = Duration.ofHours(10);
    private final static int UPLOAD_THRESHOLD = 1;
    private final ComponentConnection<Triple<Switch, Account, Location<LocationConfig>>> locationConfigQueue;
    private final Map<String, Pair<Account, Location<LocationConfig>>> urlToLocation;
    private AtomicBoolean on;
    private final PriorityComponentConnection<Post> contentStreamerQueue;
    private final ContentRepository contentRepository;
    private final PostsTimeline postsTimeline;
    private final ComponentConnection<Post> streamerErrorQueue;

    public Solver(ComponentConnection<Triple<Switch, Account, Location<LocationConfig>>> locationConfigQueue,
                  PriorityComponentConnection<Post> contentStreamerQueue,
                  ContentRepository contentRepository,
                  PostsTimeline postsTimeline, ComponentConnection<Post> streamerErrorQueue, AtomicBoolean on) {
        this.locationConfigQueue = locationConfigQueue;
        this.contentStreamerQueue = contentStreamerQueue;
        this.contentRepository = contentRepository;
        this.postsTimeline = postsTimeline;
        this.streamerErrorQueue = streamerErrorQueue;
        this.on = on;
        this.urlToLocation = new HashMap<>();
    }

    public void run() {

        while (on.get()) {
            try {
                Triple<Switch, Account, Location<LocationConfig>> locationSwitch = locationConfigQueue.poll();
                while (locationSwitch != null) {
                    if (locationSwitch.getLeft() == Switch.ENABLE) {
                        urlToLocation.put(locationSwitch.getRight().url.toString(),
                                new ImmutablePair<>(locationSwitch.getMiddle(), locationSwitch.getRight()));
                    } else {
                        urlToLocation.remove(locationSwitch.getRight().url.toString());
                    }
                    locationSwitch = locationConfigQueue.poll();
                }
                for (Pair<Account, Location<LocationConfig>> accoundAndLocation : urlToLocation.values()) {
                    Account account = accoundAndLocation.getLeft();
                    Location<LocationConfig> location = accoundAndLocation.getRight();
                    ConcurrentLinkedQueue<Post> posts = postsTimeline.getPosts(location.url.toString());
                    for (Instant instant : generateNewPostInstants(location.locationConfig, posts,
                            UPLOAD_THRESHOLD)) {
                        Content content = null;
                        try {
                            content = contentRepository.getContent(location.locationConfig.precision,
                                    location.locationConfig.tags, account, location, postsTimeline);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        Post post = new Post(instant, content, account, location);
                        if (content != null) {
                            contentStreamerQueue.offer(post);
                        } else {
                            // todo notify content-manager
                        }
                        postsTimeline.addPost(location.url.toString(), post);
                    }
                }
                Post postWithAlreadyPostedContent = streamerErrorQueue.poll();
                while (postWithAlreadyPostedContent != null) {
                    Content content = contentRepository.getContent(
                            postWithAlreadyPostedContent.location.locationConfig.precision,
                            postWithAlreadyPostedContent.location.locationConfig.tags,
                            postWithAlreadyPostedContent.account, postWithAlreadyPostedContent.location, postsTimeline);
                    if (content != null) {
                        Post post = new Post(postWithAlreadyPostedContent.time, content,
                                postWithAlreadyPostedContent.account, postWithAlreadyPostedContent.location);
//                        postWithAlreadyPostedContent.content = content;
                        contentStreamerQueue.offer(post);
                        postsTimeline.addPost(postWithAlreadyPostedContent.location.url.toString(), post);
                    }
                    postWithAlreadyPostedContent = streamerErrorQueue.poll();
                }
                sleep(1000);
            } catch (Throwable e) {
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
