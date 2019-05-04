package lol.lolpany.postamatik.youtube;

import lol.lolpany.postamatik.Content;
import lol.lolpany.postamatik.PostsTimeline;
import lol.lolpany.postamatik.SourceInputStream;
import org.zeroturnaround.exec.ProcessExecutor;

import java.io.File;
import java.io.FilenameFilter;
import java.util.UUID;

import static lol.lolpany.postamatik.Postamatik.POSTAMATIK_HOME;
import static lol.lolpany.postamatik.Postamatik.YOUTUBE_DL;

public class YoutubeDlInputStream implements SourceInputStream {

    private final String source;
    private final Content content;
    private final String videoCache;
    private final PostsTimeline postsTimeline;
    private final String locationUrl;

    YoutubeDlInputStream(String source, Content content, String videoCache, PostsTimeline postsTimeline, String locationUrl) {
        this.source = source;
        this.content = content;
        this.videoCache = videoCache;
        this.postsTimeline = postsTimeline;
        this.locationUrl = locationUrl;
    }

    @Override
    public Content read() throws Exception {
        String fileName = UUID.randomUUID().toString();

        content.name = new ProcessExecutor().readOutput(true).command(
                YOUTUBE_DL,
                "--no-check-certificate", "-e", source)
                .execute().outputString("windows-1251");

        if (postsTimeline.isAlreadyUploadedOrPosted(locationUrl, content)) {
            return content;
        }

        new ProcessExecutor().command(YOUTUBE_DL,
                "--no-check-certificate", "-f", "\"bestvideo+bestaudio/best\"", "-o", videoCache + "\\" + fileName,
                source).execute();

        File root = new File(videoCache);
        FilenameFilter beginswithm = (directory, filename) -> filename.startsWith(fileName);

        File[] files = root.listFiles(beginswithm);
        if (files.length > 0) {
            content.file = root.listFiles(beginswithm)[0];
            return content;
        } else {
            return null;
        }
    }
}
