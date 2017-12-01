package lol.lolpany.postamatik.youtube;

import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.VideoStatus;
import lol.lolpany.postamatik.PostAction;

import java.io.IOException;

public class YoutubePostAction implements PostAction {
    final String videoId;
    private final YouTube youTube;

    public YoutubePostAction(String videoId, YouTube youTube) {
        this.videoId = videoId;
        this.youTube = youTube;
    }

    @Override
    public void run() {
        try {
            youTube.videos().update("status", new com.google.api.services.youtube.model.Video().setId(videoId).setStatus(
                    new VideoStatus().setPrivacyStatus("public")
            )).execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
