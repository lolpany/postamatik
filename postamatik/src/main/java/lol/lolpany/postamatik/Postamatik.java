package lol.lolpany.postamatik;

import com.codeborne.selenide.Configuration;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lol.lolpany.*;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;

import java.io.File;
import java.io.FileReader;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.codeborne.selenide.WebDriverRunner.closeWebDriver;
import static java.util.Comparator.comparing;

public class Postamatik {

    public static final boolean HEADLESS = true;

    public static final String CLIENT_ID = "1059174167186-g08vc81kulft2qs4dq9ndokcjl297469.apps.googleusercontent.com";
    public static final String TEST_CLIENT_ID = "718851251848-esnfeglm7kbjvej8hm52hcrq7qlsrlll.apps.googleusercontent.com";
    private static final String AUTHORIZATION_SERVER_ENCODED_URL = "https://accounts.google.com/o/oauth2/v2/auth";

    final static int CONTENT_STREAMER_MAXIMUM_PARALLER_STREAMS = 5;

//    public static final String POSTAMATIK_HOME = "/home/user/postamatik/";
//    public final static String CHROME_DRIVER_LOCATION = POSTAMATIK_HOME + "chromedriver";
//    public final static String GECKO_DRIVER_LOCATION = POSTAMATIK_HOME + "geckodriver";
//    public static final String CONFIG_DIR = POSTAMATIK_HOME + "config/";
//    public static final String PUBLIC_CONFIG_DIR = POSTAMATIK_HOME + "content-repository";
//    public static final String ACCOUNTS_CONFIG = CONFIG_DIR + "accounts-config.json";
//    public static final String POSTS_TIMELINE = CONFIG_DIR + "posts-timeline.json";
//    public static final String YOUTUBE_DL_DIR = "/usr/local/bin/";
//    public static final String YOUTUBE_DL = YOUTUBE_DL_DIR + "youtube-dl";
//    public static final String FFMPEG_DIR = "/usr/bin/";
//    public static final String FFMPEG = FFMPEG_DIR + "ffmpeg";
//    public final static String VIDEO_CACHE = "/tmp/postamatik-cache";
//    public static final String CREDENTIAL_STORAGE = POSTAMATIK_HOME + "access-token";
//    public static final File CLIENT_SECRET = new File(POSTAMATIK_HOME + "clientSecret.txt");


    public static final String POSTAMATIK_HOME = "R:\\postamatik\\postamatik\\";
    public final static String GECKO_DRIVER_LOCATION = "R:\\geckodriver.exe";
    public final static String CHROME_DRIVER_LOCATION = "R:\\chromedriver.exe";
    public static final String CONFIG_DIR = "C:\\all\\info\\buffer\\postamatik\\accounts-config";
//    public static final String CONFIG_DIR = "C:\\all\\info\\buffer\\postamatik-test\\accounts-config";
    public static final String PUBLIC_CONFIG_DIR = "C:\\Users\\user\\Dropbox\\projects\\postamatik-public-config\\content-repository\\";
    public static final String ACCOUNTS_CONFIG = CONFIG_DIR + "\\accounts-config.json";
    public static final String POSTS_TIMELINE = POSTAMATIK_HOME + "posts-timeline.json";
    public static final String YOUTUBE_DL_DIR = "R:\\";
    public static final String YOUTUBE_DL = YOUTUBE_DL_DIR + "youtube-dl.exe";
    public static final String FFMPEG_DIR = YOUTUBE_DL_DIR;
    public static final String FFMPEG = FFMPEG_DIR + "ffmpeg.exe";
    public final static String VIDEO_CACHE = "R:\\postamatik-cache\\";
    public static final String CREDENTIAL_STORAGE = "C:\\all\\info\\buffer\\postamatik\\access-token";
    public static final String TEST_CREDENTIAL_STORAGE = "C:\\all\\info\\buffer\\postamatik\\access-token";
    public static final File CLIENT_SECRET = new File("C:\\all\\info\\buffer\\postamatik\\clientSecret.txt");
    public static final File TEST_CLIENT_SECRET = new File("C:\\all\\info\\buffer\\postamatik\\clientSecret.txt");


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
        ComponentConnection<Triple<Switch, Account, Location<LocationConfig>>> locationConfigSolverQueue = new ComponentConnection<>(100);
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
        if (new File(POSTS_TIMELINE).exists()) {
            postsTimeline =
                    gson.fromJson(
                            new FileReader(POSTS_TIMELINE),
                            PostsTimeline.class
                    );
        } else {
        postsTimeline = new PostsTimeline(accountsConfig);
        }

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
                PUBLIC_CONFIG_DIR,
                contentRepositoryStoreQueue, "content-repository-store.json", gson, isOn));

        ContentRepository contentRepository = new ContentRepository(contentRepositoryStoreQueue, postsTimeline
        );

        executorService.execute(new Component(isOn, new LocationSwitcher(accountsConfigsQueue, locationConfigSolverQueue)));

        executorService.execute(
                new Solver(locationConfigSolverQueue, contentStreamerQueue,
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
