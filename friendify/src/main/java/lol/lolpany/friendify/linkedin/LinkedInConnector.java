package lol.lolpany.friendify.linkedin;

import com.codeborne.selenide.*;
import lol.lolpany.Account;
import lol.lolpany.Location;
import lol.lolpany.friendify.Connector;
import lol.lolpany.friendify.LocationConfig;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import javax.mail.*;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.codeborne.selenide.Selenide.*;
import static com.codeborne.selenide.WebDriverRunner.setWebDriver;
import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.NANOSECONDS;

public class LinkedInConnector implements Connector {

    public final static String CHROME_DRIVER_LOCATION = "D:\\storage\\Dropbox\\Dropbox\\projects\\postamatik\\bin\\chromedriver.exe";
    private static final long MAIL_RECEIVE_INTERVAL = MINUTES.convert(4, NANOSECONDS);
    private static final String PIN_MAIL_SUBJECT = ", here's your PIN";
    private static final Pattern PIN_PATTERN = Pattern.compile("Please use this verification code to complete your sign in: (\\d+)");
    private static final String CONTACT_BUTTON_SELECTOR = "button.search-result__actions--primary";

    private final AtomicBoolean isOn;
    private final Account<LocationConfig> account;
    private final Location<LocationConfig> location;

    private static final long TIMEOUT = 180000;



    public LinkedInConnector(AtomicBoolean isOn, Account<LocationConfig> account, Location<LocationConfig> location) {
        this.isOn = isOn;
        this.account = account;
        this.location = location;
    }

    @Override
    public Void call() {
        int startPage = 1;
        int endPage = 100;

        Configuration.timeout = TIMEOUT;

//        System.setProperty("webdriver.gecko.driver", "D:\\buffer\\geckodriver-v0.19.1-win64\\geckodriver.exe");

//        DesiredCapabilities capabilities = DesiredCapabilities.firefox();
//        capabilities.setCapability("marionette", true);
//        capabilities.setCapability(org.openqa.selenium.remote.CapabilityType.PROXY, p);

//        FirefoxProfile profile = new FirefoxProfile();
//        profile.setPreference("network.proxy.type", 1);
//        profile.setPreference("network.proxy.socks", "127.0.0.1");
//        profile.setPreference("network.proxy.socks_port", 9050);


//        FirefoxOptions firefoxOptions = new FirefoxOptions();
//        firefoxOptions.setProfile(profile);
//        firefoxOptions.addArguments("-headless");
//        firefoxOptions.addCapabilities(capabilities);
//
//        setWebDriver(new FirefoxDriver(firefoxOptions));
//        Configuration.baseUrl = "https://www.linkedin.com/";

        System.setProperty("webdriver.chrome.driver", CHROME_DRIVER_LOCATION);
        ChromeOptions chromeOptions = new ChromeOptions();
//        chromeOptions.addArguments("headless");
        chromeOptions.addArguments("--window-size=1600,1000");
        chromeOptions.addArguments("--proxy-server=socks5://127.0.0.1:9050");
        setWebDriver(new ChromeDriver(chromeOptions));

        UrlIterator urlIterator = new UrlIterator(location.locationConfig.tags);

        try {
            login();

//        Selenide.sleep(20000);

            while (isOn.get()) {
                try {
                    String url = location.url.toString();
                    while (urlIterator.hasNext()) {
                        iteratePages(url + urlIterator.next(), startPage, endPage);
                    }
                } catch (Throwable e) {
                    // ignore
                }
                Thread.sleep(Math.round(location.locationConfig.frequency * TimeUnit.DAYS.toMillis(1)));
            }
        } catch (MessagingException | IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }



    public boolean login() throws IOException, javax.mail.MessagingException {
        open("https://www.linkedin.com/uas/login");
        $("#session_key-login").sendKeys(this.account.login);
        $("#session_password-login").sendKeys(this.account.password);
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
        $("div.left-rail-container").should(Condition.visible);
        return true;
    }

    public String readVerificationCodeFromMail() throws IOException, javax.mail.MessagingException {
        Properties props = System.getProperties();
        props.setProperty("mail.store.protocol", "imaps");
        Session session = Session.getDefaultInstance(props, null);
        Store store = session.getStore("imaps");
        store.connect("imap.gmail.com", this.account.login, this.account.password);

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

    void iteratePages(String url, int startPage, int endPage) {
        for (int i = startPage; i < endPage && isOn.get(); i++) {
            try {
                System.out.println(i);
                open(url + "&page=" + i);
//                JavascriptExecutor jse = (JavascriptExecutor) getWebDriver();


//                Selenide.sleep(15000);
//
//                jse.executeScript("window.scrollBy(0,500)", "");
//
//                Selenide.sleep(5000);


                while (!$("li.page-list").is(Condition.visible)) {
                    $$(CONTACT_BUTTON_SELECTOR).shouldBe(CollectionCondition.sizeGreaterThan(0)).last().scrollTo();
                }

                ElementsCollection buttons = $$(CONTACT_BUTTON_SELECTOR);


                int count = 0;
//                            buttons.get(0).scrollTo();
                for (int j = 0; j < buttons.size(); j++) {
//                    jse.executeScript("windo\w.scrollBy(0,1000)", "");
                    SelenideElement button = buttons.get(j);
                    if (button != null) {
                        if ("Connect".equals(button.getText())) {
                            System.out.println("Connect");
                            count++;
                            try {
                                if (j == 0) {
                                    $("button.search-filters-bar__clear-filters").scrollTo();
                                }
                                if (j > 0) {
                                    buttons.get(j - 1).scrollTo();
                                }
                                SelenideElement sendButton =
                                        $("div.modal-wormhole-content div.send-invite__actions button.button-primary-large");
                                if (!sendButton.exists()) {
                                    button.should(Condition.visible).click();
                                }
                                if (!$("input#email").exists()) {
                                    $("div.modal-wormhole-content div.send-invite__actions button.button-primary-large")
                                            .should(Condition.visible).click();
                                } else {
                                    $("button.send-invite__cancel-btn").click();
                                }
//                                            Configuration.timeout = 5000;
//                                            try {
//
//                                            } catch (SelenideTime)
                                long waitForInvitationClose = 0;
                                while (waitForInvitationClose < 3000 && $("div.modal-wormhole-content div.send-invite__actions button.button-primary-large").is(Condition.visible)) {
                                    waitForInvitationClose += 100;
                                    sleep(100);
                                }
                                if ($("div.modal-wormhole-content div.send-invite__actions button.button-primary-large")
                                        .is(Condition.visible)) {
                                    $("button.send-invite__cancel-btn").click();
                                }
//                                            $("div.modal-wormhole-content div.send-invite__actions button.button-primary-large")
//                                                    .should(Condition.not(Condition.visible));
//
//                                            Configuration.timeout = TIMEOUT;
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
}
