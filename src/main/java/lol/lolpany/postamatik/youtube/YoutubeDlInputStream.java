package lol.lolpany.postamatik.youtube;

import lol.lolpany.postamatik.Content;
import lol.lolpany.postamatik.SourceInputStream;
import org.zeroturnaround.exec.ProcessExecutor;

import java.io.File;
import java.util.UUID;

public class YoutubeDlInputStream implements SourceInputStream {

    private final String source;
    private final Content content;
    private final String videoCache;

    YoutubeDlInputStream(String source, Content content, String videoCache) {
        this.source = source;
        this.content = content;
        this.videoCache = videoCache;
    }

    @Override
    public Content read() throws Exception {
        String filePath = "\"" + videoCache + "\\" + UUID.randomUUID().toString() + ".mp4" + "\"";

        content.name = new ProcessExecutor().readOutput(true).command(
                "D:\\storage\\Dropbox\\projects\\postamatik\\resource\\youtube-dl.exe",
                "--no-check-certificate", "-e", source).execute().outputString("windows-1251");

        new ProcessExecutor().command("D:\\storage\\Dropbox\\projects\\postamatik\\resource\\youtube-dl.exe",
                "--no-check-certificate", "-o", filePath, source).execute();
        content.file = new File(filePath);

        return content;
    }
}
