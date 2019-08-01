package lol.lolpany.postamatik.pornhub;

import com.codeborne.selenide.Configuration;
import com.google.api.client.googleapis.media.MediaHttpUploader;
import com.google.api.client.http.InputStreamContent;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.VideoSnippet;
import com.google.api.services.youtube.model.VideoStatus;
import lol.lolpany.Account;
import lol.lolpany.postamatik.Content;
import lol.lolpany.postamatik.LocationOutputStream;
import lol.lolpany.postamatik.PostAction;
import lol.lolpany.postamatik.Utils;
import lol.lolpany.postamatik.youtube.YoutubeApi;
import lol.lolpany.postamatik.youtube.YoutubeDesignation;
import lol.lolpany.postamatik.youtube.YoutubeLocation;
import lol.lolpany.postamatik.youtube.YoutubePostAction;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.function.Function;
import java.util.regex.Pattern;

import static java.lang.Thread.sleep;
import static lol.lolpany.postamatik.Postamatik.CHROME_DRIVER_LOCATION;
import static lol.lolpany.postamatik.Postamatik.HEADLESS;
import static org.openqa.selenium.By.cssSelector;

public class PornhubOutputStream implements LocationOutputStream {

    private final Account account;
    private final PornhubLocation location;

    public PornhubOutputStream(Account account, PornhubLocation location) {
        this.account = account;
        this.location = location;

    }

    @Override
    public PostAction write(Content content) throws IOException, GeneralSecurityException {

        System.setProperty("webdriver.chrome.driver", CHROME_DRIVER_LOCATION);
        Configuration.timeout = 60000;
        ChromeOptions chromeOptions = new ChromeOptions();
        chromeOptions.addArguments("--no-sandbox");
        if (HEADLESS) {
            chromeOptions.setHeadless(true);
        }
        WebDriver webDriver = new ChromeDriver(chromeOptions);
        try {
            webDriver.get("https://pornhub.com/");
            webDriver.findElement(cssSelector("#js-checkAge")).click();
            for (String windowHandle : webDriver.getWindowHandles()) {
                webDriver.switchTo().window(windowHandle);
                if (webDriver.getTitle().contains("Вход")) {
                    break;
                }
            }
            webDriver.findElement(cssSelector("input[name='email']")).sendKeys("79670207451");
            webDriver.findElement(cssSelector("input[name='pass']")).sendKeys("er234sddsf3");
            webDriver.findElement(cssSelector("#install_allow")).click();
            for (String windowHandle : webDriver.getWindowHandles()) {
                webDriver.switchTo().window(windowHandle);
                if (webDriver.getTitle().contains("Порно")) {
                    break;
                }
            }
            webDriver.get("https://www.pornhub.com/upload");
            webDriver.findElement(cssSelector("#username")).sendKeys(account.login);
            webDriver.findElement(cssSelector("#password")).sendKeys(account.password);
            try {
                sleep(5000);

                webDriver.findElement(cssSelector("#submit")).click();
                sleep(5000);
                webDriver.get("https://rt.pornhub.com/upload");
                webDriver.findElement(cssSelector("#videoUploadLink")).click();
                sleep(1000);
                webDriver.findElement(cssSelector("#fileUploadField")).sendKeys(content.file.getAbsolutePath());
                sleep(1000);
                webDriver.findElement(cssSelector("#titleTmplField_0")).sendKeys(content.name);
                webDriver.findElement(cssSelector("#orientation_1_Grouped #categoryId_5_0 span")).click();
                webDriver.findElement(cssSelector("#tagsList_0")).sendKeys("nude");
                webDriver.findElement(cssSelector("#submitNewTag_0")).click();
                webDriver.findElement(cssSelector("#tagsList_0")).sendKeys("sexy");
                webDriver.findElement(cssSelector("#submitNewTag_0")).click();
                webDriver.findElement(cssSelector("#uploaderSaveButton_0")).click();
                while (!webDriver.findElement(cssSelector("#uploadingText_0")).getText().contains("succes")) {
                    sleep(10000);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } finally {
            webDriver.quit();
        }
        return null;
    }

}
