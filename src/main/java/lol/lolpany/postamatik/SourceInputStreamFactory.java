package lol.lolpany.postamatik;

import java.io.FileNotFoundException;

public interface SourceInputStreamFactory {

    SourceInputStream create(String source, Content content) throws FileNotFoundException, InterruptedException;
}
