package lol.lolpany.postamatik;

import com.codeborne.selenide.Configuration;
import org.apache.commons.io.FileUtils;
import org.junit.Test;

import java.io.File;

import static lol.lolpany.postamatik.ContentStreamerDispatcher.VIDEO_CACHE;

public class OnlinevideoconverterInputStreamTest {
    @Test
    public void go() throws Exception {
        Configuration.timeout = 60000;
        FileUtils.copyFile(new OnlinevideoconverterInputStreamFactory("D:\\buffer\\chromedriver\\chromedriver.exe",
                        VIDEO_CACHE).
                        create("https://www.youtube.com/watch?v=utuxLmZyvzA", new Content(null, null, null)).read().file,
                new File("D:\\buffer\\go.go"));
    }
}
