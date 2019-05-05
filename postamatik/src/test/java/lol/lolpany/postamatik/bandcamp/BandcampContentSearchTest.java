package lol.lolpany.postamatik.bandcamp;

import lol.lolpany.Location;
import lol.lolpany.postamatik.Content;
import lol.lolpany.postamatik.ContentLength;
import lol.lolpany.postamatik.LocationConfig;
import lol.lolpany.postamatik.PostsTimeline;
import org.junit.Test;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.net.MalformedURLException;
import java.net.URL;

import static com.codeborne.selenide.WebDriverRunner.setWebDriver;
import static java.util.Collections.singleton;
import static java.util.Collections.singletonList;
import static lol.lolpany.postamatik.Postamatik.CHROME_DRIVER_LOCATION;
import static lol.lolpany.postamatik.Postamatik.HEADLESS;

public class BandcampContentSearchTest {
    @Test
    public void test() throws MalformedURLException {
        System.setProperty("webdriver.chrome.driver", CHROME_DRIVER_LOCATION);
        ChromeOptions chromeOptions = new ChromeOptions();
        chromeOptions.addArguments("--no-sandbox");
        if (HEADLESS) {
            chromeOptions.setHeadless(true);
        }
        setWebDriver(new ChromeDriver(chromeOptions));
        Content content = new BandcampContentSearch("https://bandcamp.com/tag/lo-fi?sort_field=pop", singleton("lo-fi")).findContent(1.0, singleton("lo-fi"), new PostsTimeline(),
                null, new Location<>(new URL("http://www.lol.lol"), new LocationConfig(null, null, 0.0, 0.0, 3,
                        singletonList(ContentLength.LONG))));
        int a = 1;
    }
}
