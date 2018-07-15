package lol.lolpany.friendify.linkedin;

import org.junit.Test;

import java.util.HashSet;

public class UrlIteratorTest {
    @Test
    public void test() {
        UrlIterator urlIterator = new UrlIterator(new HashSet<String>() {{
            add("us");
            add("bb");
            add("ca");
        }});
        while (urlIterator.hasNext()) {
            System.out.println(urlIterator.next());
        }
    }
}
