package lol.lolpany.postamatik.youtube;

import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.SearchResult;
import org.apache.commons.io.FileUtils;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;

import static lol.lolpany.postamatik.TestUtils.TEST_ACCOUNT;
import static lol.lolpany.postamatik.TestUtils.testYoutubeLocation;
import static lol.lolpany.postamatik.youtube.YoutubeContentSearch.VIDEO_PREFIX;

public class YoutubeApiTest {
    private static final String POP_VIDEOS_FILE_LOCATION = "D:\\buffer\\youtube-top.txt";

    @Test
    public void go() throws IOException, GeneralSecurityException {
        StringBuilder result = new StringBuilder();
        String pageToken = "";
        for (int i = 0; i < 100; i++) {
            SearchListResponse search = YoutubeApi.fetchYouTube(TEST_ACCOUNT, testYoutubeLocation).search().list("snippet").setOrder("viewCount")
                    .setPageToken(pageToken).setType("video").setMaxResults(50L).execute();
            for (SearchResult searchResult : search.getItems()) {
                result.append(VIDEO_PREFIX + searchResult.getId().getVideoId() + " - " + searchResult.getSnippet().getTitle() + " - " + searchResult.getSnippet().getDescription());
            }
            pageToken = search.getNextPageToken();
        }
        FileUtils.writeStringToFile(new File(POP_VIDEOS_FILE_LOCATION), result.toString(), StandardCharsets.UTF_8.toString());
    }
}
