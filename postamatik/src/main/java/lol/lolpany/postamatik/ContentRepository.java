package lol.lolpany.postamatik;

import lol.lolpany.Account;
import lol.lolpany.ComponentConnection;
import lol.lolpany.Location;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.Set;

import static com.codeborne.selenide.Browsers.CHROME;
import static com.codeborne.selenide.WebDriverRunner.closeWebDriver;
import static com.codeborne.selenide.WebDriverRunner.setWebDriver;
import static lol.lolpany.postamatik.Postamatik.CHROME_DRIVER_LOCATION;
import static lol.lolpany.postamatik.Postamatik.HEADLESS;

public class ContentRepository {

    private final ComponentConnection<ContentRepositoryStore> contentRepositoryStoreQueue;
    private ContentRepositoryStore contentRepositoryStore;
    private final PostsTimeline postsTimeline;

    public ContentRepository(ComponentConnection<ContentRepositoryStore> contentRepositoryStoreQueue,
                             PostsTimeline postsTimeline) {
        this.contentRepositoryStoreQueue = contentRepositoryStoreQueue;
        this.postsTimeline = postsTimeline;

        System.setProperty("webdriver.chrome.driver", CHROME_DRIVER_LOCATION);
        ChromeOptions chromeOptions = new ChromeOptions();
        if (HEADLESS) {
            chromeOptions.setHeadless(true);
        }
    }

    Content getContent(double precision, Set<String> tags, Account account, Location location, PostsTimeline timeline)
            throws IOException, GeneralSecurityException {
        try {
            ContentRepositoryStore newContentRepositoryStore = contentRepositoryStoreQueue.poll();
            if (newContentRepositoryStore != null) {
                contentRepositoryStore = newContentRepositoryStore;
            }

            if (contentRepositoryStore != null) {
                for (Content content : contentRepositoryStore.contentList) {
                    if (!timeline.isAlreadyScheduledOrUploadedOrPosted(location.url.toString(), content)
                            && Utils.match(tags, content.tags) >= precision) {
                        return content;
                    }
                }
                Collections.shuffle(contentRepositoryStore.contentSearchList);
                for (ContentSearch contentSearch : contentRepositoryStore.contentSearchList) {
                    Content content = contentSearch.findContent(precision, tags, postsTimeline, account, location);
                    if (content != null) {
                        if (!timeline.isAlreadyScheduledOrUploadedOrPosted(location.url.toString(), content)
                                && Utils.match(tags, content.tags) >= precision) {
                            return content;
                        }
                    }
                }
            }
        } finally {
            closeWebDriver();
        }
        return null;
    }
}
