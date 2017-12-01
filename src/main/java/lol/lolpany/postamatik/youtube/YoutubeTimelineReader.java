package lol.lolpany.postamatik.youtube;

import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.*;
import com.google.api.services.youtube.model.Video;
import lol.lolpany.postamatik.*;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;

import static java.lang.Math.min;

public class YoutubeTimelineReader implements LocationTimelineReader<YoutubeLocation> {

    private static final int FETCH_SIZE = 50;


    @Override
    public ConcurrentLinkedQueue<Post> read(Account account, YoutubeLocation location) {

        ConcurrentLinkedQueue<Post> result = new ConcurrentLinkedQueue<>();


        try {
            YouTube youTube = YoutubeUtils.fetchYouTube(account, location);
            Channel channel = youTube.channels().list("snippet,contentDetails").setMine(true).execute().getItems().stream()
                    .filter(ch -> ch.getSnippet().getTitle().equals(location.channelName)).collect(Collectors.toList())
                    .get(0);
            String uploadsId = channel.getContentDetails().getRelatedPlaylists().getUploads();


            List<Video> videos = fetchUploadedVideos(youTube, fetchUploadedVideosIds(youTube, uploadsId));

            for (Video video : videos) {
                Content content = new Content(null, null, null);
                content.name = video.getSnippet().getTitle();
                Post post = new Post(Instant.ofEpochMilli(video.getSnippet().getPublishedAt().getValue()), content,
                        account, location);
                if (!video.getStatus().getUploadStatus().equals("failed")
                        && !video.getStatus().getUploadStatus().equals("rejected")) {
                    if (video.getStatus().getPrivacyStatus().equals("private")) {
                        post.postState = PostState.UPLOADED;
                        post.setAction(new YoutubePostAction(video.getId(), youTube));
                    } else {
                        post.postState = PostState.POSTED;
                    }
                    result.add(post);
                }
            }

        } catch (IOException | GeneralSecurityException e) {
            e.printStackTrace();
        }
        return result;
    }

    private List<String> fetchUploadedVideosIds(YouTube youTube, String uploadsId) throws IOException {

        List<PlaylistItem> uploadedVideos = new ArrayList<>();

        YouTube.PlaylistItems.List playlistItemListRequest = youTube.playlistItems().list("contentDetails")
                .setMaxResults((long) FETCH_SIZE)
                .setPlaylistId(uploadsId);

        PlaylistItemListResponse playlistItemListResponse = playlistItemListRequest.execute();
        uploadedVideos.addAll(playlistItemListResponse.getItems());

        while (playlistItemListResponse.getNextPageToken() != null) {
            playlistItemListResponse = playlistItemListRequest.setPageToken(playlistItemListResponse.getNextPageToken())
                    .execute();
            uploadedVideos.addAll(playlistItemListResponse.getItems());
        }

        return uploadedVideos.stream()
                .map(playlistItem -> playlistItem.getContentDetails().getVideoId())
                .collect(Collectors.toList());
    }

    private List<Video> fetchUploadedVideos(YouTube youTube, List<String> videosIds) throws IOException {
        List<Video> result = new ArrayList<>();

        int from = 0;
        int to = min(videosIds.size(), FETCH_SIZE);
        while (from < videosIds.size()) {
            YouTube.Videos.List videoListRequest = youTube.videos().list("snippet,status")
                    .setMaxResults((long) FETCH_SIZE)
                    .setId(StringUtils.join(videosIds.subList(from, to), ","));
            VideoListResponse videoListResponse = videoListRequest.execute();
            result.addAll(videoListResponse.getItems());

            while (videoListResponse.getNextPageToken() != null) {
                videoListResponse = videoListRequest.setPageToken(videoListResponse.getNextPageToken())
                        .execute();
                result.addAll(videoListResponse.getItems());
            }

            from = to;
            to = min(videosIds.size(), to + FETCH_SIZE);
        }

        return result;
    }

}
