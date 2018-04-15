package lol.lolpany.postamatik;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentSkipListSet;

import static lol.lolpany.postamatik.PostsTimeline.isPostPresent;

public class ContentStreamerDispatcherCache {

    ConcurrentHashMap<String, ConcurrentLinkedQueue<Post>> cache;

    public ContentStreamerDispatcherCache() {
        cache = new ConcurrentHashMap<>();
    }

    public boolean isPosting(String url, Content content) {
        return isPostPresent(cache, url, content);
    }

    public void put(String source, Post post) {
        cache.computeIfAbsent(source, (key) -> new ConcurrentLinkedQueue<>()).add(post);
    }

//    public void remove(Post post) {
//        cache.get(post.source).removeIf((p) -> p.equals(post));
//    }
}
