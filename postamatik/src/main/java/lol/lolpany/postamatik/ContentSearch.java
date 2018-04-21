package lol.lolpany.postamatik;

import java.io.IOException;
import java.net.MalformedURLException;
import java.security.GeneralSecurityException;
import java.util.Set;

public interface ContentSearch {
    Content findContent(double precision, Set<String> tags, PostsTimeline postsTimeline, Account account, Location location) throws IOException, GeneralSecurityException;
}
