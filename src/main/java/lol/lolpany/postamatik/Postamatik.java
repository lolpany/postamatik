package lol.lolpany.postamatik;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.FileReader;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.codeborne.selenide.WebDriverRunner.closeWebDriver;
import static java.util.Comparator.comparing;

public class Postamatik {

    public static void main(String[] args) throws Exception {


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

        AccountsConfig accountsConfig = gson.fromJson(
                new FileReader("D:\\storage\\info\\buffer\\postamatik\\accounts-config\\accounts-config.json"),
                AccountsConfig.class);

        PostsTimeline postsTimeline = new PostsTimeline(accountsConfig);
//                gson.fromJson(
//                        new FileReader("D:\\storage\\web-go\\resource\\posts-timeline\\posts-timeline.json"),
//                        PostsTimeline.class
//                );

//        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
//            try {
//                postsTimeline.close();
//                    posterQueue.close();
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
//        ));

//        postsTimeline.reupload(contentStreamerQueue);
        postsTimeline.repost(posterQueue, accountsConfig);


        ComponentConnection<Post> streamerErrorQueue = new ComponentConnection<>(1000);

        int numberOfRunnables = 20;

        ExecutorService executorService = new ThreadPoolExecutor(numberOfRunnables, numberOfRunnables, 5,
                TimeUnit.MINUTES, new ArrayBlockingQueue<>(numberOfRunnables));

        executorService.execute(new JsonConfigWatcher<>(AccountsConfig.class,
                "D:\\storage\\info\\buffer\\postamatik\\accounts-config",
                accountsConfigsQueue, "accounts-config.json", gson, isOn));

        executorService.execute(new JsonConfigWatcher<>(ContentRepositoryStore.class,
                "D:\\storage\\Dropbox\\projects\\postamatik\\resource\\content-repository",
                contentRepositoryStoreQueue, "content-repository-store.json", gson, isOn));

        ContentRepository contentRepository = new ContentRepository(contentRepositoryStoreQueue, postsTimeline);

        executorService.execute(new Solver(accountsConfigsQueue, contentStreamerQueue,
                contentRepository, postsTimeline, isOn));

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
