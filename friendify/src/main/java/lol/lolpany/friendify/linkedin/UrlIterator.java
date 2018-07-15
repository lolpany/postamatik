package lol.lolpany.friendify.linkedin;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import static java.lang.String.join;

public class UrlIterator implements Iterator<String> {

    private static final List<LinkedInUrlPartGeneratorFactory> LINKED_IN_URL_PART_GENERATOR_FACTORIES =
            new ArrayList<LinkedInUrlPartGeneratorFactory>() {{
                add(new GeoLinkedInUrlPartGeneratorFactory());
            }};

    List<ResettableIterator<String>> urlPartsIterators;
    List<String> currentValues;

    public UrlIterator(Set<String> tags) {
        urlPartsIterators = new ArrayList<>();
        for (LinkedInUrlPartGeneratorFactory factory : LINKED_IN_URL_PART_GENERATOR_FACTORIES) {
            for (String tag : tags) {
                if (factory.canCreate(tag)) {
                    urlPartsIterators.add(factory.create(tag));
                }
            }
        }
        this.currentValues = new ArrayList<>();
        this.currentValues.add("");
        for (int i = 1; i < urlPartsIterators.size(); i++) {
            currentValues.add(urlPartsIterators.get(i).next());
        }
    }

    @Override
    public boolean hasNext() {
        boolean result = false;
        int i = 0;
        while (i < urlPartsIterators.size() && !urlPartsIterators.get(i).hasNext()) {
//            result = urlPartsIterators.get(i).hasNext();
            i++;
        }
        return i < urlPartsIterators.size();
    }

    @Override
    public String next() {
        String result = null;
        int i = 0;

        while (i < urlPartsIterators.size() && !urlPartsIterators.get(i).hasNext()) {
            i++;
        }

        if (urlPartsIterators.get(i).hasNext()) {
            resetIterators(i);
            currentValues.set(i, urlPartsIterators.get(i).next());
            result = "&" + join("&", currentValues);
        }
        return result;
    }

    private void resetIterators(int currentIteratorIndex) {
        for (int i = 0; i < currentIteratorIndex; i++) {
            urlPartsIterators.get(i).reset();
        }
    }
}
