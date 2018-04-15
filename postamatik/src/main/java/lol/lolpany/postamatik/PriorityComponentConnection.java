package lol.lolpany.postamatik;

import java.util.Comparator;
import java.util.concurrent.PriorityBlockingQueue;

public class PriorityComponentConnection<T> {
    final PriorityBlockingQueue<T> queue;


    public PriorityComponentConnection(int capacity, Comparator<T> comparator) {
        this.queue = new PriorityBlockingQueue<>(capacity, comparator);
    }

    public T poll() {
        return queue.poll();
    }

    public void offer(T post) {
        this.queue.offer(post);
    }

    public T peek() {
        return queue.peek();
    }
}
