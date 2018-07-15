package lol.lolpany.friendify.linkedin;

import java.util.Iterator;

public interface ResettableIterator<T> extends Iterator<T> {
    void reset();
}
