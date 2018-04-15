package lol.lolpany.postamatik;

import com.google.gson.Gson;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.PriorityBlockingQueue;

import static java.util.Comparator.comparing;
import static org.apache.commons.io.FileUtils.writeStringToFile;

public class PosterQueue {

    PriorityComponentConnection<Post> queue;

    public PosterQueue(int initialCapacity) {
        queue = new PriorityComponentConnection<>(initialCapacity, Comparator.comparing(post -> post.time));
    }

//    @Override
//    public void close() throws Exception {
//        writeStringToFile(new File("D:\\storage\\web-go\\resource\\poster-queue\\poster-queue.json"),
//                new Gson().toJson(new PersistedPosts(this)), StandardCharsets.UTF_8);
//    }

    public void put(Post post) {
        this.queue.offer(post);
    }

    public Post poll() {
        return queue.poll();
    }

    public Post peek() {
        return queue.peek();
    }
}
