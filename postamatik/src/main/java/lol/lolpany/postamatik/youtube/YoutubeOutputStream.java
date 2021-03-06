package lol.lolpany.postamatik.youtube;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.googleapis.media.MediaHttpUploader;
import com.google.api.client.http.InputStreamContent;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.VideoSnippet;
import com.google.api.services.youtube.model.VideoStatus;
import lol.lolpany.Account;
import lol.lolpany.postamatik.Content;
import lol.lolpany.postamatik.LocationOutputStream;
import lol.lolpany.postamatik.PostAction;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;

public class YoutubeOutputStream implements LocationOutputStream {

    private final Account account;
    private final YoutubeLocation location;

    public YoutubeOutputStream(Account account, YoutubeLocation location) {
        this.account = account;
        this.location = location;

    }

    @Override
    public PostAction write(Content content) throws IOException, GeneralSecurityException {

        YouTube youTube = YoutubeApi.fetchYouTube(account, location, YoutubeDesignation.OUTPUT_STREAM);


        com.google.api.services.youtube.model.Video videoObjectDefiningMetadata
                = new com.google.api.services.youtube.model.Video();

        VideoStatus status = new VideoStatus();
        status.setPrivacyStatus("private");
        videoObjectDefiningMetadata.setStatus(status);

        VideoSnippet snippet = new VideoSnippet();
        snippet.setTitle(content.name);
//        snippet.setDescription("");
        // no tags for no bans
//        snippet.setTags(new ArrayList<>(location.locationConfig.tags));

        videoObjectDefiningMetadata.setSnippet(snippet);

        InputStreamContent mediaContent = new InputStreamContent("video/*",
                new FileInputStream(content.file));

        YouTube.Videos.Insert videoInsert = youTube.videos()
                .insert("snippet,statistics,status", videoObjectDefiningMetadata, mediaContent);
        MediaHttpUploader uploader = videoInsert.getMediaHttpUploader();
        uploader.setDisableGZipContent(true);
//        uploader.setDirectUploadEnabled(true);
        uploader.setChunkSize(4194304);
        com.google.api.services.youtube.model.Video returnedVideo = videoInsert.execute();
        return new YoutubePostAction(returnedVideo.getId(), youTube, account, location);
    }

}
