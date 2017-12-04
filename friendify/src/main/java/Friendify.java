import com.codeborne.selenide.*;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.remote.DesiredCapabilities;

import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Store;
import java.io.IOException;
import java.util.Date;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.codeborne.selenide.Selenide.*;
import static com.codeborne.selenide.WebDriverRunner.getWebDriver;
import static com.codeborne.selenide.WebDriverRunner.setWebDriver;
import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.NANOSECONDS;

public class Friendify {

    private static final long MAIL_RECEIVE_INTERVAL = MINUTES.convert(4, NANOSECONDS);
    private static final String PIN_MAIL_SUBJECT = ", here's your PIN";
    private static final Pattern PIN_PATTERN = Pattern.compile("Please use this verification code to complete your sign in: (\\d+)");
    private static final String CONTACT_BUTTON_SELECTOR = "button.search-result__actions--primary";

    //    @Test
    public static void main(String[] args) throws Exception {
        int startPage = 1;
        int endPage = 200;

        Configuration.timeout = 60000;

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
//        firefoxOptions.addArguments("-headless");
        firefoxOptions.addCapabilities(capabilities);

        setWebDriver(new FirefoxDriver(firefoxOptions));
//        Configuration.baseUrl = "https://www.linkedin.com/";

        if (!login()) {
            throw new Exception();
        }
//        Selenide.sleep(20000);

        for (int i = startPage; i < endPage; i++) {
            try {
                System.out.println(i);
                open("https://www.linkedin.com/search/results/people/?facetGeoRegion=%5B%22us%3A0%22%5D&facetNetwork=%5B%22S%22%5D&origin=FACETED_SEARCH&page=" + i);
//                JavascriptExecutor jse = (JavascriptExecutor) getWebDriver();


//                Selenide.sleep(15000);
//
//                jse.executeScript("window.scrollBy(0,500)", "");
//
//                Selenide.sleep(5000);
                while (!$("li.page-list").is(Condition.visible)) {
                    $$(CONTACT_BUTTON_SELECTOR).last().scrollTo();
                }

                ElementsCollection buttons = $$(CONTACT_BUTTON_SELECTOR);


                for (int j = 0; j < buttons.size(); j++) {
//                    jse.executeScript("window.scrollBy(0,1000)", "");
                    SelenideElement button = $$("button.search-result__actions--primary").get(j);
                    if (button != null) {
                        if ("Connect".equals(button.getText())) {
                            try {
                                button.should(Condition.exist).click();
                                SelenideElement sendButton =
                                        $("div.modal-wormhole-content div.send-invite__actions button.button-primary-large");
                                sendButton.should(Condition.visible).click();
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

    public static boolean login() throws IOException, javax.mail.MessagingException {
        open("https://www.linkedin.com/uas/login");
        $("#session_key-login").sendKeys("gbesergey@gmail.com");
        $("#session_password-login").sendKeys("!@#$f23$Gfdfs3");
        $("#btn-primary").submit();
        if (!$$("#pagekey-uas-consumer-ato-pin-challenge").isEmpty()) {
            String pin = readVerificationCodeFromMail();
            if (pin != null) {
                $("#verification-code").sendKeys(pin);
                $("#btn-primary").submit();
            } else {
                return false;
            }
        }
        $("li#profile-nav-item").should(Condition.visible);
        return true;
    }

    public static String readVerificationCodeFromMail() throws IOException, javax.mail.MessagingException {
        Properties props = System.getProperties();
        props.setProperty("mail.store.protocol", "imaps");
        Session session = Session.getDefaultInstance(props, null);
        Store store = session.getStore("imaps");
        store.connect("imap.gmail.com", "gbesergey@gmail.com", "seBon-nouveau@($)");

        Folder inbox = store.getFolder("Inbox");
        try {
            inbox.open(Folder.READ_ONLY);
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
