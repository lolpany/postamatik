package lol.lolpany.postamatik;

import com.codeborne.selenide.SelenideElement;

import java.time.Instant;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.sleep;
import static java.time.temporal.ChronoUnit.HOURS;

public class SelenideUtils {

    private static final long SLEEP_STEP = 200;
    private static final long DEFAULT_SLEEP_TIME = 10000;

    public static void waitTill(Supplier<Boolean> condition) {
        waitTill(condition, DEFAULT_SLEEP_TIME);
    }

    public static void waitTill(Supplier<Boolean> condition, long timeout) {
        long sleepTime = 0;
        while (!condition.get() && sleepTime < timeout) {
            sleep(SLEEP_STEP);
            sleepTime += SLEEP_STEP;
        }
    }

    public static String getText(SelenideElement element) {
        return element.innerText();
    }

    public static boolean isDaysPassed(LocationConfig locationConfig, Content content) {
        return locationConfig.daysPassedLimit != null && locationConfig.daysPassedLimit > 0 &&
                content.time.isBefore(Instant.now()
                        .minus(Math.round(Math
                                        .ceil(locationConfig.daysPassedLimit * TimeUnit.HOURS.convert(1, TimeUnit.DAYS))),
                                HOURS));
    }
}
