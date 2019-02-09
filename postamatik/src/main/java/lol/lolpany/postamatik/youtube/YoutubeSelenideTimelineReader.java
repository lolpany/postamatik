package lol.lolpany.postamatik.youtube;

import com.codeborne.selenide.CollectionCondition;
import com.codeborne.selenide.Condition;
import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.SelenideElement;
import com.google.api.services.youtube.YouTube;
import lol.lolpany.Account;
import lol.lolpany.postamatik.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

import static com.codeborne.selenide.Selenide.*;
import static com.codeborne.selenide.WebDriverRunner.closeWebDriver;
import static com.codeborne.selenide.WebDriverRunner.setWebDriver;
import static lol.lolpany.postamatik.Postamatik.HEADLESS;
import static lol.lolpany.postamatik.SelenideUtils.getText;

public class YoutubeSelenideTimelineReader implements LocationTimelineReader<YoutubeLocation> {

    private final static Set<String> VIDEO_ERROR_NOTIFICATIONS = new HashSet<String>() {{
        add("Video removed: Terms of Service violation");
//        add("Upload failed: Duplicate upload");
        add("Upload failed: Video too long");
        add("Upload failed: Can't process file");
        add("Видео удалено (нарушение Условий использования)");
//        add("Ошибка (повторная загрузка)");
        add("Ошибка (видео слишком длинное)");
        add("Ошибка при загрузке (не удалось обработать файл)");
    }};

    private final String chromeDriverLocation;

    public YoutubeSelenideTimelineReader(String chromeDriverLocation) {
        this.chromeDriverLocation = chromeDriverLocation;
    }

    @Override
    public ConcurrentLinkedQueue<Post> read(Account account, YoutubeLocation location) {

        ConcurrentLinkedQueue<Post> result = new ConcurrentLinkedQueue<>();

        System.setProperty("webdriver.chrome.driver", chromeDriverLocation);
        ChromeOptions chromeOptions = new ChromeOptions();
        if (HEADLESS) {
            chromeOptions.addArguments("headless");
        }
        setWebDriver(new ChromeDriver(chromeOptions));

        YoutubeUtils.authorize(account, location);

        YouTube youTube = null;
        try {
            youTube = YoutubeApi.fetchYouTube(account, location);
        } catch (IOException | GeneralSecurityException e) {
            e.printStackTrace();
        }

        int i = 1;
        open("https://www.youtube.com/my_videos?o=U&pi=" + i);
        while (!$("div.vm-no-items").is(Condition.visible)) {

            $$("div.vm-video-item-content").shouldBe(CollectionCondition.sizeGreaterThan(0));
            ElementsCollection elements = $$("div.vm-video-item-content");
            for (SelenideElement element : elements) {
                SelenideElement videoContainer = element.should(Condition.exist).scrollTo();
                SelenideElement selenideElement = element.find("span.vm-video-side-notification-text-item");
                if (!selenideElement.exists() || !VIDEO_ERROR_NOTIFICATIONS.contains(selenideElement.getText())) {
                    SelenideElement videoTitle = videoContainer.find(".vm-video-title-content");
                    String videoName = getText(videoContainer.find(".vm-video-title-content"));
                    SelenideElement postTime = videoContainer.find("span.vm-date-info");
                    Instant videoPostTime = null;
                    if (postTime.exists()) {
                        try {
                            videoPostTime = LocalDateTime.parse(postTime.getText(),
                                    DateTimeFormatter.ofPattern("MMM d, y h:m a")).toInstant(OffsetDateTime.now().getOffset());
                        } catch (DateTimeParseException e) {

                        }
                    }
                    Boolean isPublishButton = videoContainer.find("button.vm-video-publish").exists();
                    Content content = new Content(null, null, null);
                    content.name = videoName;
                    Post post = new Post(videoPostTime, content, account, location);
                    post.postState = isPublishButton ? PostState.UPLOADED : PostState.POSTED;
                    if (isPublishButton) {
                        post.postState = PostState.UPLOADED;
                        post.setAction(new YoutubePostAction(
                                videoTitle.attr("href").split("video_id=")[1], youTube, account, location));
                    } else {
                        post.postState = PostState.POSTED;
                    }
                    result.add(post);
                }
            }
            i++;
            open("https://www.youtube.com/my_videos?o=U&pi=" + i);
        }
        closeWebDriver();
        return result;
    }

}
