import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lol.lolpany.AccountsConfig;
import lol.lolpany.Location;
import lol.lolpany.postamatik.*;
import org.junit.Test;
import org.zeroturnaround.exec.ProcessExecutor;

import java.io.File;
import java.io.FileReader;
import java.util.Arrays;
import java.util.Objects;
import java.util.UUID;

import static lol.lolpany.postamatik.Postamatik.ACCOUNTS_CONFIG;
import static lol.lolpany.postamatik.Postamatik.FFMPEG;

public class FfmpegUtils {

    @Test
    public void readFromLocations() throws Exception {
        String thumb = "thumb.jpg";
        String folder = "R:\\postamatik-cache";
        File root = new File(folder);
        StringBuilder concatOption = new StringBuilder("concat:");
        for (File audio : listFiles(root)) {
            concatOption.append(audio).append("|");
        }
        concatOption.setLength(concatOption.length() - 1);

        String videoFileName = UUID.randomUUID().toString() + ".mp4";

//        new ProcessExecutor().command(FFMPEG, "-i",
//                concatOption.toString(), "-loop", "1", "-r", "1", "-i",
//                folder + File.separator + thumb, "-c", "copy", "-shortest", "-vcodec", "libx264",
//                "-metadata", "title=" + contentName,
//                folder + File.separator + videoFileName).execute();

        String result = FFMPEG + " -i " + " \"" +
                concatOption + "\" " + " -loop " + " 1 " + " -r " + " 1 " + " -i " +
                folder + File.separator + thumb + " -c " + " copy " + " -shortest " + " -vcodec " + " libx264 " +
//                "-metadata" + "title=\"" + contentName + "\"" +
                folder + File.separator + videoFileName;
        System.out.println(result);
    }

    private File[] listFiles(File root) {
        File[] result = root.listFiles((dir, name) -> name.endsWith(".mp3"));
        if (result.length == 0) {
            result = root.listFiles((dir, name) -> name.endsWith(".aiff"));
        }
        if (result.length == 0) {
            result = root.listFiles((dir, name) -> name.endsWith(".wav"));
        }
        Arrays.sort(result);
        return result;
    }
}
