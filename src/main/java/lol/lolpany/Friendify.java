package lol.lolpany;

import com.codeborne.selenide.Configuration;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.remote.DesiredCapabilities;

import javax.mail.*;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.codeborne.selenide.Selenide.sleep;
import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.NANOSECONDS;
import static javax.mail.Folder.READ_ONLY;

public class Friendify {

    private static final long MAIL_RECEIVE_INTERVAL = MINUTES.convert(4, NANOSECONDS);
    private static final String PIN_MAIL_SUBJECT = ", here's your PIN";
    private static final Pattern PIN_PATTERN = Pattern.compile("Please use this verification code to complete your sign in: (\\d+)");


//    @Test
    public static void main(String[] args) throws Exception {
        int startPage = 1;
        int endPage = 200;

        System.setProperty("webdriver.gecko.driver", "D:\\buffer\\geckodriver-v0.17.0-win64\\geckodriver.exe");

        DesiredCapabilities capabilities = DesiredCapabilities.firefox();
        capabilities.setCapability("marionette", true);
//        capabilities.setCapability(org.openqa.selenium.remote.CapabilityType.PROXY, p);

        FirefoxProfile profile = new FirefoxProfile();
        profile.setPreference("network.proxy.type", 1);
        profile.setPreference("network.proxy.socks", "127.0.0.1");
        profile.setPreference("network.proxy.socks_port", 9050);


        FirefoxOptions firefoxOptions = new FirefoxOptions();
        firefoxOptions.setProfile(profile);
        firefoxOptions.addCapabilities(capabilities);

        WebDriver driver = new FirefoxDriver(firefoxOptions);
        Configuration.baseUrl = "https://www.linkedin.com/";

        if (!login(driver)) {
            throw new Exception();
        }
        sleep(20000);

        for (int i = startPage; i < endPage; i++) {
            try {
                System.out.println(i);
                driver.get("https://www.linkedin.com/search/results/people/?facetGeoRegion=%5B%22us%3A0%22%5D&facetNetwork=%5B%22S%22%5D&origin=FACETED_SEARCH&page=" + i);
                JavascriptExecutor jse = (JavascriptExecutor) driver;

//
//            new WebDriverWait(driver, 10).until((ExpectedCondition<Boolean>) wd ->
//                    ((JavascriptExecutor) wd).executeScript("return document.readyState").equals("complete"));

                sleep(15000);

                jse.executeScript("window.scrollBy(0,500)", "");

                sleep(5000);


                List<WebElement> buttons = driver.findElements(By.className("search-result__actions--primary"));


                for (int j = 0; j < buttons.size(); j++) {
                    jse.executeScript("window.scrollBy(0,1000)", "");
                    WebElement button = driver.findElements(By.className("search-result__actions--primary")).get(j);
                    if (button != null) {
                        System.out.println(button.getText());
                        if ("Connect".equals(button.getText())) {
                            try {
                                button.click();
                                WebElement connectDiaglog = null;
                                while (connectDiaglog == null) {
                                    connectDiaglog = driver.findElement(By.className("modal-wormhole-content"));
                                }

                                WebElement sendButton = null;
                                while (sendButton == null) {
                                    sendButton = connectDiaglog.findElement(By.className("send-invite__actions"))
                                            .findElement(By.className("button-primary-large"))
                                    ;
                                }
                                sendButton.submit();
                                sleep(5000);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }

                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static boolean login(WebDriver driver) throws MessagingException, IOException {
        driver.get("https://www.linkedin.com/uas/login");
        driver.findElement(By.id("session_key-login")).sendKeys("gbesergey@gmail.com");
        driver.findElement(By.id("session_password-login")).sendKeys("!@#$f23$Gfdfs3");
        driver.findElement(By.id("btn-primary")).submit();
        if (!driver.findElements(By.id("pagekey-uas-consumer-ato-pin-challenge")).isEmpty()) {
            String pin = readVerificationCodeFromMail();
            if (pin != null) {
                driver.findElement(By.id("verification-code")).sendKeys(pin);
                driver.findElement(By.id("btn-primary")).submit();
            } else {
                return false;
            }
        }
        return true;
    }

    public static String readVerificationCodeFromMail() throws MessagingException, IOException {
        Properties props = System.getProperties();
        props.setProperty("mail.store.protocol", "imaps");
        Session session = Session.getDefaultInstance(props, null);
        Store store = session.getStore("imaps");
        store.connect("imap.gmail.com", "gbesergey@gmail.com", "seBon-nouveau@($)");

        Folder inbox = store.getFolder("Inbox");
        try {
            inbox.open(READ_ONLY);
            while (true) {
                long now = new Date().getTime();
                for (Message message : inbox.getMessages()) {
                    long sendTime = message.getSentDate().getTime();
                    if (Math.abs(now - sendTime) < MAIL_RECEIVE_INTERVAL) {
                        if (message.getSubject().endsWith(PIN_MAIL_SUBJECT)) {
                            Matcher pinMatcher = PIN_PATTERN.matcher((CharSequence) message.getContent());
                            if (pinMatcher.find()) {
                                return pinMatcher.group(1);
                            } else {
                                return null;
                            }
                        }
                    }
                }
                return null;
            }
        } finally {
            inbox.close(false);
        }
    }
}
