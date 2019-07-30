package lol.lolpany.postamatik.pornhub;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lol.lolpany.AccountsConfig;
import lol.lolpany.Location;
import lol.lolpany.postamatik.*;
import lol.lolpany.postamatik.youtube.YoutubeContentSearch;
import org.junit.Test;

import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.security.GeneralSecurityException;

import static java.util.Collections.singleton;
import static lol.lolpany.postamatik.TestUtils.*;

public class PornhubContentSearchTest {

    @Test
    public void testSearchChannel() throws IOException, InterruptedException {
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(PostAction.class, new PostActionDeserializer())
                .registerTypeAdapter(Location.class, new LocationDeserializer())
                .registerTypeAdapter(ContentSearch.class, new ContentSearchDeserializer())
                .setPrettyPrinting()
                .create();

        System.out.println(new PornhubContentSearch("https://www.pornhub.com/video?o=mv&t=t",
                singleton("test"))
                .findContent(1, singleton("test"), new PostsTimeline(
                        gson.fromJson(
                                new FileReader(ACCOUNTS_CONFIG),
                                AccountsConfig.class
                        )), TEST_PORNHUB_ACCOUNT, testPornhubLocation).actualSources);
    }
}
