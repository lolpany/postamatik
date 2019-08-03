package lol.lolpany.postamatik.pornhub;

import com.codeborne.selenide.Configuration;
import lol.lolpany.Account;
import lol.lolpany.postamatik.Content;
import lol.lolpany.postamatik.LocationOutputStream;
import lol.lolpany.postamatik.PostAction;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.io.IOException;
import java.security.GeneralSecurityException;

import static java.lang.Thread.sleep;
import static lol.lolpany.postamatik.Postamatik.CHROME_DRIVER_LOCATION;
import static lol.lolpany.postamatik.Postamatik.HEADLESS;
import static lol.lolpany.postamatik.pornhub.PornhubUtils.login;
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
            login(webDriver, account);
            webDriver.get("https://rt.pornhub.com/upload");
            sleep(1000);
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

        } finally {
            webDriver.quit();
        }
        return null;
    }

}
