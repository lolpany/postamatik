package lol.lolpany.postamatik.youtube;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lol.lolpany.postamatik.*;
import lol.lolpany.postamatik.youtube.YoutubeContentSearch;
import lol.lolpany.postamatik.youtube.YoutubeLocation;
import org.junit.Test;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.net.MalformedURLException;
import java.net.URL;

import static java.util.Collections.singleton;

public class YoutubeContentSearchTest {

    @Test
    public void go() throws MalformedURLException, FileNotFoundException {
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(PostAction.class, new PostActionDeserializer())
                .registerTypeAdapter(Location.class, new LocationDeserializer())
                .registerTypeAdapter(ContentSearch.class, new ContentSearchDeserializer())
                .setPrettyPrinting()
                .create();

        System.out.println(new YoutubeContentSearch("https://www.youtube.com/channel/UCSJ4gkVC6NrvII8umztf0Ow/videos",
                singleton("lol"))
                .findContent(1, singleton("lol"), new PostsTimeline(
                                gson.fromJson(
                                        new FileReader("D:\\storage\\web-go\\resource\\accounts-config\\accounts-config.json"),
                                        AccountsConfig.class
                                )),
                        new YoutubeLocation(new URL("https://www.youtube.com/channel/UCcpxdjh8t3e65jGqdiADYWA"),
                                new LocationConfig(null, null, 0, 0), "Electronic Music")).actualSources);
    }

}
