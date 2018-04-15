package lol.lolpany.postamatik;

import java.net.MalformedURLException;
import java.util.Set;

public interface ContentSearch {
    Content findContent(double precision, Set<String> tags, PostsTimeline postsTimeline, Location location) throws MalformedURLException;
}
