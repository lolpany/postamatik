package lol.lolpany.postamatik.youtube;

import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.PlaylistItem;
import com.google.api.services.youtube.model.PlaylistItemListResponse;
import lol.lolpany.postamatik.*;

import java.io.IOException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.time.Duration;
import java.time.format.DateTimeFormatter;
import java.util.Set;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
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
    public Content findContent(double precision, Set<String> tags, PostsTimeline postsTimeline, Account account, Location location)
            throws IOException, GeneralSecurityException {
        Content result = null;
        if (Utils.match(this.tags, tags) >= precision) {
            result = findContent(account, url, tags, postsTimeline, (YoutubeLocation) location);
        }
        return result;
    }

    private Content findContent(Account account, String url, Set<String> tags, PostsTimeline postsTimeline,
                                YoutubeLocation location) throws IOException, GeneralSecurityException {
        YouTube youTube = YoutubeApi.fetchYouTube(account, location);
        String uploadsPlaylistId = "";
        URL contentSourceUrl = new URL(url);
        if (contentSourceUrl.getPath().startsWith(CHANNEL)) {
            uploadsPlaylistId = youTube.channels().list("contentDetails").setId(contentSourceUrl.getPath().split("/")[2])
                    .execute().getItems().get(0).getContentDetails().getRelatedPlaylists().getUploads();
        } else if (contentSourceUrl.getPath().startsWith(USER)) {
            uploadsPlaylistId = youTube.channels().list("contentDetails").setForUsername(contentSourceUrl.getPath().split("/")[2])
                    .execute().getItems().get(0).getContentDetails().getRelatedPlaylists().getUploads();
        } else if (contentSourceUrl.getPath().startsWith(PLAYLIST)) {
            uploadsPlaylistId = contentSourceUrl.getQuery().substring(5);
        }
        String nextPageToken = "";
        while (nextPageToken != null) {
            PlaylistItemListResponse response = youTube.playlistItems().list("snippet,contentDetails")
                    .setPlaylistId(uploadsPlaylistId).setMaxResults((long) FETCH_SIZE).setPageToken(nextPageToken).execute();
            for (PlaylistItem playlistItem : response.getItems()) {
                if (isContentLengthSuitable(youTube, playlistItem.getContentDetails().getVideoId(), location.locationConfig.contentLength)) {
                    Content content = new Content(tags, singletonList(VIDEO_PREFIX + playlistItem.getContentDetails().getVideoId()), emptyList());
                    content.name = playlistItem.getSnippet().getTitle();
                    if (!postsTimeline.isAlreadyScheduledOrUploadedOrPosted(location.url.toString(), content)) {
                        return content;
                    }
                }
            }
            nextPageToken = response.getNextPageToken();
        }
        return null;
    }

    private boolean isContentLengthSuitable(YouTube youTube, String videoId, ContentLength locationContentLength) throws IOException {
        return ContentLength.fromMinutes(Duration.parse(youTube.videos().list("contentDetails").setId(videoId).execute().getItems().get(0).getContentDetails().getDuration()).toMinutes()) ==
                locationContentLength;
    }
}