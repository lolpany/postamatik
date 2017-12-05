package lol.lolpany.postamatik.youtube;

import com.google.api.client.googleapis.media.MediaHttpUploader;
import com.google.api.client.http.InputStreamContent;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.VideoSnippet;
import com.google.api.services.youtube.model.VideoStatus;
import lol.lolpany.postamatik.Account;
import lol.lolpany.postamatik.Content;
import lol.lolpany.postamatik.LocationOutputStream;
import lol.lolpany.postamatik.PostAction;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;

import static com.google.api.client.googleapis.media.MediaHttpUploader.MINIMUM_CHUNK_SIZE;

public class YoutubeOutputStream implements LocationOutputStream {

    private final Account account;
    private final YoutubeLocation location;

    public YoutubeOutputStream(Account account, YoutubeLocation location) {
        this.account = account;
        this.location = location;

    }

    @Override
    public PostAction write(Content content) throws IOException, GeneralSecurityException {

        YouTube youTube = YoutubeUtils.fetchYouTube(account, location);



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
        uploader.setDisableGZipContent(false);
        uploader.setDirectUploadEnabled(false);
        com.google.api.services.youtube.model.Video returnedVideo = videoInsert.execute();

        return new YoutubePostAction(returnedVideo.getId(), youTube, account, location);
    }

}
