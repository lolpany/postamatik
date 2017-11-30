package lol.lolpany.postamatik;

import java.util.Set;

import static java.lang.Math.max;
import static org.apache.commons.lang3.StringUtils.equalsIgnoreCase;

public class Utils {
    public static double match(Set<String> oneTags, Set<String> anotherTags) {
        double result = 0;
        double partWeight = 1.0 / max(oneTags.size(), anotherTags.size());
        for (String oneTag : oneTags) {
            for (String anotherTag : anotherTags) {
                if (equalsIgnoreCase(oneTag, anotherTag)) {
                    result += partWeight;
                }
            }
        }
        return result;
    }
}
