package lol.lolpany.postamatik;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;

public interface LocationOutputStream {
    PostAction write(Content content) throws IOException, GeneralSecurityException;
}
