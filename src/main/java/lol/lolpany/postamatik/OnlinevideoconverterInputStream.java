package lol.lolpany.postamatik;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.SelenideElement;
import org.apache.commons.io.FileUtils;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URL;

import static com.codeborne.selenide.Selenide.*;
import static com.codeborne.selenide.WebDriverRunner.closeWebDriver;
import static com.codeborne.selenide.WebDriverRunner.setWebDriver;

public class OnlinevideoconverterInputStream implements SourceInputStream {
    private final String chromeDriverLocation;
    private final String source;
    private final Content content;
    private final String videoCache;

    public OnlinevideoconverterInputStream(String chromeDriverLocation, String source, Content content, String videoCache)
            throws FileNotFoundException, InterruptedException {
        this.chromeDriverLocation = chromeDriverLocation;
        this.source = source;
        this.content = content;
        this.videoCache = videoCache;
    }

    @Override
    public Content read() throws Exception {
        System.setProperty("webdriver.chrome.driver", chromeDriverLocation);
        ChromeOptions chromeOptions = new ChromeOptions();
        chromeOptions.addArguments("headless");
        setWebDriver(new ChromeDriver(chromeOptions));
        open("https://www.onlinevideoconverter.com/ru/video-converter");
        $("#texturl").sendKeys(source);
        $("div#select_main").click();
        $("div#select_main a[data-value=\"mp4\"]").should(Condition.exist).click();
        $("#convert1").click();
        SelenideElement downloadButton = $("#downloadq");
        downloadButton.should(Condition.visible).should(Condition.attribute("href"));
//        File file = downloadButton.download();
        File file = new File(videoCache + $("div.download-section-1-1-title-content a").attr("title")
                .replaceAll("[^a-zA-Z0-9\\.\\-]", "_") + ".mp4");
        FileUtils.copyURLToFile(new URL(downloadButton.attr("href")), file);
        content.name = $("div.download-section-1-1-title-content a").attr("title");
        close();
        closeWebDriver();
        content.file = file;
        return content;
    }
}
