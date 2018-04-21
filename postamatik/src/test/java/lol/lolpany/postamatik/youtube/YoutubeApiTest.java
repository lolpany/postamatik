package lol.lolpany.postamatik.youtube;

import org.junit.Test;

import java.io.IOException;
import java.security.GeneralSecurityException;

import static lol.lolpany.postamatik.TestUtils.TEST_ACCOUNT;
import static lol.lolpany.postamatik.TestUtils.testYoutubeLocation;

public class YoutubeApiTest {
    @Test
    public void go() throws IOException, GeneralSecurityException {
        YoutubeApi.fetchYouTube(TEST_ACCOUNT, testYoutubeLocation).channels().list("contentDetails")
                .setId("UCC2VdQa8i5_4GiW446zhPug").execute().getItems().get(0).getContentDetails()
                .getRelatedPlaylists().getUploads();
    }
}
