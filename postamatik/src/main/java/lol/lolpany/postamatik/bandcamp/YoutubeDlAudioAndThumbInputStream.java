package lol.lolpany.postamatik.bandcamp;

import lol.lolpany.postamatik.Content;
import lol.lolpany.postamatik.PostsTimeline;
import lol.lolpany.postamatik.SourceInputStream;
import org.zeroturnaround.exec.ProcessExecutor;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.UUID;

import static lol.lolpany.postamatik.Postamatik.FFMPEG;
import static lol.lolpany.postamatik.Postamatik.YOUTUBE_DL;

public class YoutubeDlAudioAndThumbInputStream implements SourceInputStream {

    private final String source;
    private final Content content;
    private final String videoCache;
    private final PostsTimeline postsTimeline;
    private final String locationUrl;

    YoutubeDlAudioAndThumbInputStream(String source, Content content, String videoCache, PostsTimeline postsTimeline,
                                      String locationUrl) {
        this.source = source;
        this.content = content;
        this.videoCache = videoCache;
        this.postsTimeline = postsTimeline;
        this.locationUrl = locationUrl;
    }

    @Override
    public Content read() throws Exception {
        String folderName = UUID.randomUUID().toString();

        String folder = videoCache + File.separator + folderName;
        Files.createDirectories(Paths.get(folder));


        if (postsTimeline.isAlreadyUploadedOrPosted(locationUrl, content)) {
            return content;
        }

        new ProcessExecutor().command(YOUTUBE_DL, "--restrict-filenames",
                "--no-check-certificate", "-f", "mp3", "--write-thumbnail", "-o",
                folder + File.separator + "%(title)s.%(ext)s", source).execute();

        File root = new File(folder);
        String thumb = Objects.requireNonNull(root.listFiles((dir, name) -> name.endsWith(".jpg")))[0].getName();
        File audio = root.listFiles((dir, name) -> name.endsWith(".mp3"))[0];

        String videoFileName = UUID.randomUUID().toString() + ".avi";

        new ProcessExecutor().command(FFMPEG,
                "-loop", "1", "-r", "1", "-i",
                folder + File.separator + thumb, "-i", audio.getAbsolutePath(), "-c", "copy", "-shortest",
                folder + File.separator + videoFileName).execute();

        content.file = root.listFiles((directory, filename) -> filename.endsWith(videoFileName))[0];

        return content;
    }
}
