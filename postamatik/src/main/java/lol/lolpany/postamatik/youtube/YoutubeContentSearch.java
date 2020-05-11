package lol.lolpany.postamatik.youtube;

import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.PlaylistItem;
import com.google.api.services.youtube.model.PlaylistItemListResponse;
import com.google.api.services.youtube.model.Video;
import lol.lolpany.Account;
import lol.lolpany.Location;
import lol.lolpany.postamatik.*;

import java.io.IOException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static lol.lolpany.postamatik.SelenideUtils.isDaysPassed;
import static lol.lolpany.postamatik.youtube.YoutubeUtils.FETCH_SIZE;

public class YoutubeContentSearch implements ContentSearch {

    private static final String CHANNEL = "/channel";
    private static final String USER = "/user";
    private static final String PLAYLIST = "/playlist";
    public static final String VIDEO_PREFIX = "https://www.youtube.com/watch?v=";

    private final String url;
    private final Set<String> tags;

    YoutubeContentSearch(String url, Set<String> tags) {
        this.url = url;
        this.tags = tags;
    }

    @Override
    public Content findContent(double precision, Set<String> tags, PostsTimeline postsTimeline, Account account,
                               Location<LocationConfig> location)
            throws IOException, GeneralSecurityException {
        Content result = null;
        if (Utils.match(this.tags, tags) >= precision) {
            result = findContent(account, url, tags, postsTimeline, location);
        }
        return result;

    }

    private Content findContent(Account account, String url, Set<String> tags, PostsTimeline postsTimeline,
                                Location location) throws IOException, GeneralSecurityException {
        YoutubeLocation youtubeLocation = (YoutubeLocation) location;
        YouTube youTube = YoutubeApi.fetchYouTube(account, youtubeLocation, YoutubeDesignation.CONTENT_SEARCH);
        String uploadsPlaylistId = "";
        URL contentSourceUrl = new URL(url);
        if (contentSourceUrl.getPath().startsWith(CHANNEL)) {
            uploadsPlaylistId =
                    youTube.channels().list("contentDetails").setId(contentSourceUrl.getPath().split("/")[2])
                            .execute().getItems().get(0).getContentDetails().getRelatedPlaylists().getUploads();
        } else if (contentSourceUrl.getPath().startsWith(USER)) {
            uploadsPlaylistId =
                    youTube.channels().list("contentDetails").setForUsername(contentSourceUrl.getPath().split("/")[2])
                            .execute().getItems().get(0).getContentDetails().getRelatedPlaylists().getUploads();
        } else if (contentSourceUrl.getPath().startsWith(PLAYLIST)) {
            uploadsPlaylistId = contentSourceUrl.getQuery().substring(5);
        }
        String nextPageToken = "";
        while (nextPageToken != null) {
            PlaylistItemListResponse response = youTube.playlistItems().list("snippet,contentDetails")
                    .setPlaylistId(uploadsPlaylistId).setMaxResults((long) FETCH_SIZE).setPageToken(nextPageToken)
                    .execute();
            Map<String, Video> idToVideo = youTube.videos().list("contentDetails").setId(response.getItems().stream()
                    .map(playlistItem -> playlistItem.getContentDetails().getVideoId())
                    .collect(Collectors.joining(",")))
                    .execute().getItems().stream().collect(Collectors.toMap(Video::getId, v -> v));
            for (PlaylistItem playlistItem : response.getItems()) {
                if (isContentLengthSuitable(idToVideo.get(playlistItem.getContentDetails().getVideoId()),
                        youtubeLocation.locationConfig.contentLengths)) {
                    Content content = new Content(tags,
                            singletonList(VIDEO_PREFIX + playlistItem.getContentDetails().getVideoId()), emptyList());
                    content.name = playlistItem.getSnippet().getTitle();
                    content.time = Instant.parse(playlistItem.getSnippet().getPublishedAt().toStringRfc3339());
                    if (!postsTimeline.isAlreadyScheduledOrUploadedOrPosted(youtubeLocation.url.toString(), content)
                            && !isDaysPassed(youtubeLocation.locationConfig, content)) {
                        return content;
                    }
                }
            }
            nextPageToken = response.getNextPageToken();
        }
        return null;
    }

    private boolean isContentLengthSuitable(Video video, List<ContentLength> locationContentLength) {
        return locationContentLength.contains(ContentLength.fromMinutes(Duration.parse(
                video.getContentDetails().getDuration() ).toMinutes()));
    }
}