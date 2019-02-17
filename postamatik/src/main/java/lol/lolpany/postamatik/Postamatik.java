package lol.lolpany.postamatik;

import com.codeborne.selenide.Configuration;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lol.lolpany.AccountsConfig;
import lol.lolpany.ComponentConnection;
import lol.lolpany.JsonConfigWatcher;
import lol.lolpany.Location;

import java.io.FileReader;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.codeborne.selenide.WebDriverRunner.closeWebDriver;
import static java.util.Comparator.comparing;
import static lol.lolpany.postamatik.ContentStreamerDispatcher.CHROME_DRIVER_LOCATION;

public class Postamatik {

    public static final boolean HEADLESS = true;

    public static final String POSTAMATIK_HOME = "R:\\postamatik\\postamatik\\";
    public static final String CONFIG_DIR = "D:\\storage\\info\\buffer\\postamatik\\accounts-config";
    public static final String PUBLIC_CONFIG_DIR = "D:\\storage\\Dropbox\\Dropbox\\projects\\postamatik-public-config\\";
    public static final String ACCOUNTS_CONFIG = CONFIG_DIR + "\\accounts-config.json";
    public static final String POSTS_TIMELINE = "D:\\storage\\info\\buffer\\postamatik\\posts-timeline\\posts-timeline.json";

    public static void main(String[] args) throws Exception {

        Configuration.timeout = 30000;


        Gson gson = new GsonBuilder()
                .registerTypeAdapter(PostAction.class, new PostActionDeserializer())
                .registerTypeAdapter(Location.class, new LocationDeserializer())
                .registerTypeAdapter(ContentSearch.class, new ContentSearchDeserializer())
                .setPrettyPrinting()
                .create();

        AtomicBoolean isOn = new AtomicBoolean(true);


        ComponentConnection<AccountsConfig> accountsConfigsQueue = new ComponentConnection<>(1);
        ComponentConnection<ContentRepositoryStore> contentRepositoryStoreQueue = new ComponentConnection<>(1);
//        PriorityBlockingQueue<Post> contentStreamerQueue = new PriorityBlockingQueue<>(1000,
//                comparing(firstPost -> firstPost.time));


        PriorityComponentConnection<Post> contentStreamerQueue = new PriorityComponentConnection<>(1000,
                comparing(firstPost -> firstPost.time));

        PosterQueue posterQueue = new PosterQueue(1000/*, gson.fromJson(
                new FileReader("D:\\storage\\web-go\\resource\\poster-queue\\poster-queue.json"),
                PersistedPosts.class)*/);

        AccountsConfig<LocationConfig> accountsConfig = gson.fromJson(
                new FileReader(ACCOUNTS_CONFIG),
                AccountsConfig.class);

        PostsTimeline postsTimeline;
//        if (new File(POSTS_TIMELINE).exists()) {
//            postsTimeline =
//                    gson.fromJson(
//                            new FileReader(POSTS_TIMELINE),
//                            PostsTimeline.class
//                    );
//        } else {
            postsTimeline = new PostsTimeline(accountsConfig);
//        }

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                postsTimeline.close();
//                    posterQueue.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        ));

//        postsTimeline.reupload(contentStreamerQueue);
        postsTimeline.repost(posterQueue, accountsConfig);


        ComponentConnection<Post> streamerErrorQueue = new ComponentConnection<>(1000);

        int numberOfRunnables = accountsConfig.accountsConfig.size() * 2 + 10;

        ExecutorService executorService = new ThreadPoolExecutor(numberOfRunnables, numberOfRunnables, 5,
                TimeUnit.MINUTES, new ArrayBlockingQueue<>(numberOfRunnables));

        executorService.execute(new JsonConfigWatcher<>(AccountsConfig.class,
                CONFIG_DIR,
                accountsConfigsQueue, "accounts-config.json", gson, isOn));

        executorService.execute(new JsonConfigWatcher<>(ContentRepositoryStore.class,
                PUBLIC_CONFIG_DIR + "content-repository",
                contentRepositoryStoreQueue, "content-repository-store.json", gson, isOn));

        ContentRepository contentRepository = new ContentRepository(contentRepositoryStoreQueue, postsTimeline,
                CHROME_DRIVER_LOCATION);

        executorService.execute(new Solver(accountsConfigsQueue, contentStreamerQueue,
                contentRepository, postsTimeline, streamerErrorQueue, isOn));

        executorService.execute(new ContentStreamerDispatcher(isOn, contentStreamerQueue, posterQueue,
                streamerErrorQueue, postsTimeline));

        executorService.execute(new Component(isOn, new Poster(posterQueue, postsTimeline)));

        int a = 0;
        while (a != 99) {
            a = System.in.read();
        }
        isOn.set(false);
        executorService.shutdown();
        closeWebDriver();

    }

}
