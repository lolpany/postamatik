package lol.lolpany.postamatik;

import java.io.File;
import java.io.IOException;

public interface LocationOutputStream {
    PostAction write(Content content) throws IOException;
}
