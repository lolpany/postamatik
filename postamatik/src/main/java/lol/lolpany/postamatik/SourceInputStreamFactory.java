package lol.lolpany.postamatik;

import java.io.FileNotFoundException;

public interface SourceInputStreamFactory {

    SourceInputStream create(String source, Content content, PostsTimeline postsTimeline, String locationUrl) throws FileNotFoundException, InterruptedException;
}
