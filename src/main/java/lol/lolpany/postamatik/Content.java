package lol.lolpany.postamatik;

import org.apache.commons.lang3.ObjectUtils;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import static org.apache.commons.lang3.StringUtils.isBlank;

public class Content {
    String name;
    Set<String> tags;
    List<String> actualSources;
    List<String> notActualSources;
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
        return !isBlank(name) && !isBlank(otherContent.name) && name.equals(otherContent.name)
                || isCommonSource(this.actualSources, otherContent.actualSources)
                || isCommonSource(this.notActualSources, otherContent.notActualSources)
                ;
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }
}
