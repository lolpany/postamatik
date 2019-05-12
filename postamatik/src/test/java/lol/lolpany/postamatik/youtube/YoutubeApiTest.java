package lol.lolpany.postamatik.youtube;

import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.SearchListResponse;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.*;
import java.util.stream.Collectors;

import static java.lang.Math.min;
import static lol.lolpany.postamatik.TestUtils.TEST_ACCOUNT;
import static lol.lolpany.postamatik.TestUtils.testYoutubeLocation;

public class YoutubeApiTest {
    private static final String POP_VIDEOS_FILE_LOCATION = "D:\\buffer\\youtube-top.txt";
    private static final String TEST_ACCOUNTS_CONFIG =
            "D:\\storage\\info\\buffer\\postamatik-test\\accounts-config\\accounts-config.json";

    @Test
    public void go() throws IOException, GeneralSecurityException {
        YouTube youtube = YoutubeApi.fetchYouTube(TEST_ACCOUNT, testYoutubeLocation, YoutubeDesignation.CONTENT_SEARCH);
        List<String> ids = new ArrayList<>();
        String pageToken = "";
        int pageCount = 100;
        int pageSize = 50;
        for (int i = 0; i < pageCount; i++) {
            SearchListResponse search = youtube.search().list("id").setOrder("viewCount")
                    .setPageToken(pageToken).setType("video").setMaxResults((long) pageSize).setQ("music").execute();
            ids.addAll(
                    search.getItems().stream().map((video) -> video.getId().getVideoId()).collect(Collectors.toList()));
            pageToken = search.getNextPageToken();
            if (search.getNextPageToken() == null) {
                break;
            }
        }
        Set<Video> videos = new HashSet<>();
        for (int i = 0; i < ids.size() / pageSize; i++) {
            videos.addAll(youtube.videos().list("snippet,statistics")
                    .setId(StringUtils.join(ids.subList(pageSize * i, min(pageSize * (i + 1), ids.size())), ","))
                    .execute().getItems().stream()
                    .map((video) -> new Video(video.getId(), video.getSnippet().getTitle(),
                            video.getSnippet().getDescription().replaceAll("\n", ""),
                            video.getStatistics().getViewCount().longValue())).collect(Collectors.toList()));
        }
        StringBuilder result = new StringBuilder();
        List<Video> sortedVideos = new ArrayList<>(videos);
        sortedVideos.sort((one, another) -> Long.compare(another.viewCount, one.viewCount));
        for (Video video : sortedVideos) {
            result.append("https://www.youtube.com/watch?v=").append(video.videoId).append(" ").append(video.toString())
                    .append("\n");
        }
        FileUtils.writeStringToFile(new File(POP_VIDEOS_FILE_LOCATION), result.toString(),
                StandardCharsets.UTF_8.toString());
    }

    @Test
    public void videoInfo() throws IOException, GeneralSecurityException {
        YouTube youtube = YoutubeApi.fetchYouTube(TEST_ACCOUNT, testYoutubeLocation, YoutubeDesignation.CONTENT_SEARCH);
        List<com.google.api.services.youtube.model.Video> videos = youtube.videos()
                .list("snippet,statistics,contentDetails,fileDetails,recordingDetails,processingDetails,status,suggestions,topicDetails")
                .setId("bbwfjjyFE8I")
                .execute().getItems();
        int a = 4;
    }

    private final static class Video {
        private final String videoId;
        private final String title;
        private final String description;
        private final long viewCount;

        private Video(String videoId, String title, String description, long viewCount) {
            this.videoId = videoId;
            this.title = title;
            this.description = description;
            this.viewCount = viewCount;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Video video = (Video) o;
            return Objects.equals(videoId, video.videoId);
        }

        @Override
        public int hashCode() {

            return Objects.hash(videoId);
        }

        @Override
        public String toString() {
            return "{" +
                    " viewCount=" + viewCount +
                    ", videoId='" + videoId + '\'' +
                    ", title='" + title + '\'' +
                    ", description='" + description + '\'' +
                    '}';
        }
    }
}
