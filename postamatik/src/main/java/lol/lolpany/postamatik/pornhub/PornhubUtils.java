package lol.lolpany.postamatik.pornhub;

import lol.lolpany.Account;
import org.openqa.selenium.WebDriver;

import static java.lang.Thread.sleep;
import static org.openqa.selenium.By.cssSelector;

public class PornhubUtils {

    static void login(WebDriver webDriver, Account<PornhubLocation> account) throws InterruptedException {
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
        sleep(5000);

        webDriver.findElement(cssSelector("#submit")).click();
        sleep(5000);
    }
}
