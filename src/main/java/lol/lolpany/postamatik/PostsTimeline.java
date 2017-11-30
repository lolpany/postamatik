package lol.lolpany.postamatik;

import lol.lolpany.postamatik.youtube.YoutubeTimelineReaderFactory;

import java.time.Instant;
import java.time.Period;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;

import static lol.lolpany.postamatik.ContentStreamerDispatcher.CHROME_DRIVER_LOCATION;
import static lol.lolpany.postamatik.Solver.generateNewPostInstants;

public class PostsTimeline {

    private final static Map<String, LocationTimelineReaderFactory> LOCATION_TIMELINES_FACTORIES =
            new HashMap<String, LocationTimelineReaderFactory>() {{
                put("www.youtube.com", new YoutubeTimelineReaderFactory(CHROME_DRIVER_LOCATION));
            }};


    private ConcurrentHashMap<String, ConcurrentLinkedQueue<Post>> timeline;

    public PostsTimeline(AccountsConfig accountsConfig) {
        this.timeline = new ConcurrentHashMap<>();
        for (Account account : accountsConfig.accountsConfig) {
            for (Location location : account.locations) {
                timeline.put(location.url.toString(), LOCATION_TIMELINES_FACTORIES.get(location.url.getHost()).
                        create().read(account, location));
            }
        }
    }

    void addPost(String location, Post post) {
        timeline.putIfAbsent(location, new ConcurrentLinkedQueue<>());
        timeline.get(location).add(post);
    }

    void setUploaded(Post post) {
        timeline.get(post.location.url.toString()).stream().filter((p) -> p.equals(post)).forEach(Post::setUploaded);
    }

    void setPosted(Post post) {
        timeline.get(post.location.url.toString()).stream().filter((p) -> p.equals(post)).forEach(Post::setPosted);
    }

    ConcurrentLinkedQueue<Post> getPosts(String location) {
        return timeline.get(location);
    }

    boolean isAlreadyPosted(String location, Content content) {
        return isPostPresent(timeline, location, content);
    }

    static boolean isPostPresent(Map<String, ConcurrentLinkedQueue<Post>> postsByLocationUrl, String location, Content content) {
        if (postsByLocationUrl.get(location) != null) {
            for (Post post : postsByLocationUrl.get(location)) {
                if (content.equals(post.content)) {
                    return true;
                }
            }
        }
        return false;
    }

    boolean isAlreadyPosted(String location, String source) {
        if (timeline.get(location) != null) {
            for (Post post : timeline.get(location)) {
                if (Objects.equals(source, (post.content.getActualSource()))) {
                    return true;
                }
            }
        }
        return false;
    }

//    @Override
//    public void close() throws Exception {
//        writeStringToFile(new File("D:\\storage\\web-go\\resource\\posts-timeline\\posts-timeline.json"),
//                new Gson().toJson(this), StandardCharsets.UTF_8);
//    }

    public void reupload(PriorityComponentConnection<Post> contentStreamerQueue) {
        timeline.values().stream().flatMap(Collection::stream).filter((p) -> p.postState == PostState.SCHEDULED)
                .forEach(contentStreamerQueue::offer);
    }

    public void repost(PosterQueue posterQueue, AccountsConfig accountsConfig) {

        for (Account account : accountsConfig.accountsConfig) {
            for (Location location : account.locations) {
                List<Post> posts = this.getPosts(location.url.toString()).stream()
                        .filter(post -> post.postState == PostState.UPLOADED).collect(Collectors.toList());
                List<Instant> instants = generateNewPostInstants(location.locationConfig,
                        this.getPosts(location.url.toString()),
                        Period.ofDays((int) (1 + Math.round(1.0/ location.locationConfig.frequency)) * posts.size()
                                + 10));
                int i = 0;
                for (Post post : posts) {
                    post.time = instants.get(i);
                    i++;
                    posterQueue.put(post);
                }

            }
        }

    }


    public boolean isAlreadyScheduledOrUploadedOrPosted(String locationUrl, Content content) {
        if (timeline.get(locationUrl) != null) {
            for (Post post : timeline.get(locationUrl)) {
                if ((post.postState == PostState.SCHEDULED ||post.postState == PostState.UPLOADED
                        || post.postState == PostState.POSTED) && content.equals(post.content)) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean isAlreadyUploadedOrPosted(String locationUrl, Content content) {
        if (timeline.get(locationUrl) != null) {
            for (Post post : timeline.get(locationUrl)) {
                if ((post.postState == PostState.UPLOADED || post.postState == PostState.POSTED)
                        && content.equals(post.content)) {
                    return true;
                }
            }
        }
        return false;
    }
}
