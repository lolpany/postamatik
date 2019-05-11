package lol.lolpany.postamatik.youtube;

import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.VideoStatus;
import lol.lolpany.Account;
import lol.lolpany.postamatik.PostAction;

import java.io.IOException;
import java.security.GeneralSecurityException;

import static lol.lolpany.postamatik.youtube.YoutubeApi.fetchYouTube;

public class YoutubePostAction implements PostAction {
    final String videoId;
    private final YouTube youTube;
    private final Account account;
    private final YoutubeLocation location;

    public YoutubePostAction(String videoId, YouTube youTube, Account account, YoutubeLocation location) {
        this.videoId = videoId;
        this.youTube = youTube;
        this.account = account;
        this.location = location;
    }

    @Override
    public void run() {
        try {
            // fetch fresh
            fetchYouTube(account, location, YoutubeDesignation.POST_ACTION).videos().update("status", new com.google.api.services.youtube.model.Video().setId(videoId).setStatus(
                    new VideoStatus().setPrivacyStatus("public")
            )).execute();
        } catch (IOException | GeneralSecurityException e) {
            e.printStackTrace();
        }
    }
}
