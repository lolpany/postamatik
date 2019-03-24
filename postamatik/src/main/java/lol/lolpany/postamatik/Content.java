package lol.lolpany.postamatik;

import org.apache.commons.lang3.ObjectUtils;

import java.io.File;
import java.net.URL;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.trim;

public class Content {
    public String name;
    public Set<String> tags;
    public List<String> actualSources;
    public List<String> notActualSources;
    public Instant time;
    public File file;

    public Content(Set<String> tags, List<String> actualSources, List<String> notActualSources) {
        this.tags = tags;
        this.actualSources = actualSources;
        this.notActualSources = notActualSources;
    }

    public static boolean isCommonSource(List<String> oneSources, List<String> anotherSources) {
        if (oneSources != null && anotherSources != null) {
            for (String source : oneSources) {
                for (String otherSource : anotherSources) {
                    if (source.equals(otherSource)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public String getActualSource() {
        return actualSources != null && actualSources.size() > 0 ? actualSources.get(0) : "";
    }


    @Override
    public boolean equals(Object content) {
        Content otherContent = (Content) content;
        return otherContent != null && (!isBlank(name) && !isBlank(otherContent.name)
                && trim(name).equals(trim(otherContent.name))
                || isCommonSource(this.actualSources, otherContent.actualSources)
                || isCommonSource(this.notActualSources, otherContent.notActualSources));
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }
}
