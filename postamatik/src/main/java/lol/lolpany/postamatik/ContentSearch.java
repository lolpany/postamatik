package lol.lolpany.postamatik;

import lol.lolpany.Account;
import lol.lolpany.Location;
import org.openqa.selenium.WebDriver;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Set;

public interface ContentSearch {
    Content findContent(double precision, Set<String> tags, PostsTimeline postsTimeline, Account account, Location<LocationConfig> location) throws IOException, GeneralSecurityException;
}
