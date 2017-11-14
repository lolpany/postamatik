package lol.lolpany.postamatik;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class ComponentConnection<T> {
    private final BlockingQueue<T> queue;


    public ComponentConnection(int capacity) {
        this.queue = new ArrayBlockingQueue<>(capacity);
    }

    public T poll() {
        return queue.poll();
    }

    public void offer(T post) {
        this.queue.offer(post);
    }
}
