package lol.lolpany.postamatik.youtube;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lol.lolpany.AccountsConfig;
import lol.lolpany.Location;
import lol.lolpany.postamatik.*;
import org.junit.Test;

import java.io.FileReader;
import java.io.IOException;
import java.security.GeneralSecurityException;

import static java.util.Collections.singleton;
import static lol.lolpany.postamatik.TestUtils.ACCOUNTS_CONFIG;
import static lol.lolpany.postamatik.TestUtils.TEST_ACCOUNT;
import static lol.lolpany.postamatik.TestUtils.testYoutubeLocation;

public class YoutubeContentSearchTest {

    @Test
    public void testSearchChannel() throws IOException, GeneralSecurityException {
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(PostAction.class, new PostActionDeserializer())
                .registerTypeAdapter(Location.class, new LocationDeserializer())
                .registerTypeAdapter(ContentSearch.class, new ContentSearchDeserializer())
                .setPrettyPrinting()
                .create();

        System.out.println(new YoutubeContentSearch("https://www.youtube.com/channel/UCc7OZA-Y1zUb_-DV5_Krq2A/videos",
                singleton("lol"))
                .findContent(1, singleton("lol"), new PostsTimeline(
                        gson.fromJson(
                                new FileReader(ACCOUNTS_CONFIG),
                                AccountsConfig.class
                        )), TEST_ACCOUNT, testYoutubeLocation).actualSources);
    }

    @Test
    public void testSearchPlaylist() throws IOException, GeneralSecurityException {
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(PostAction.class, new PostActionDeserializer())
                .registerTypeAdapter(Location.class, new LocationDeserializer())
                .registerTypeAdapter(ContentSearch.class, new ContentSearchDeserializer())
                .setPrettyPrinting()
                .create();

        System.out.println(new YoutubeContentSearch("https://www.youtube.com/playlist?list=PLg80ySq95637zMRwItitZ11Ps_G0NRy-p",
                singleton("lol"))
                .findContent(1, singleton("lol"), new PostsTimeline(
                        gson.fromJson(
                                new FileReader(ACCOUNTS_CONFIG),
                                AccountsConfig.class
                        )), TEST_ACCOUNT, testYoutubeLocation).actualSources);
    }

}
