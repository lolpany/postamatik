package lol.lolpany.postamatik;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.junit.Test;

import java.io.FileReader;

import static lol.lolpany.postamatik.Postamatik.ACCOUNTS_CONFIG;
import static lol.lolpany.postamatik.Postamatik.POSTS_TIMELINE;

public class PostsTimelineTest {


    @Test
    public void readFromLocations() throws Exception {
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(PostAction.class, new PostActionDeserializer())
                .registerTypeAdapter(Location.class, new LocationDeserializer())
                .registerTypeAdapter(ContentSearch.class, new ContentSearchDeserializer())
                .setPrettyPrinting()
                .create();
        AccountsConfig accountsConfig = gson.fromJson(
                new FileReader(ACCOUNTS_CONFIG),
                AccountsConfig.class);
        new PostsTimeline(accountsConfig).close();
    }


    @Test
    public void readFromJson() throws Exception {
        PostsTimeline postsTimeline = new GsonBuilder()
                .registerTypeAdapter(PostAction.class, new PostActionDeserializer())
                .registerTypeAdapter(Location.class, new LocationDeserializer())
                .registerTypeAdapter(ContentSearch.class, new ContentSearchDeserializer())
                .setPrettyPrinting()
                .create().fromJson(new FileReader(POSTS_TIMELINE), PostsTimeline.class);
       int a = 5;
    }
}
