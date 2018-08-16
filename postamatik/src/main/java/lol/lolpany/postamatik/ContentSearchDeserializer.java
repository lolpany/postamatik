package lol.lolpany.postamatik;

import com.google.gson.*;
import lol.lolpany.postamatik.bandcamp.BandcampContentSearch;
import lol.lolpany.postamatik.youtube.YoutubeContentSearch;

import java.lang.reflect.Type;

public final class ContentSearchDeserializer implements JsonDeserializer<ContentSearch> {

    public ContentSearch deserialize(JsonElement elem, Type interfaceType, JsonDeserializationContext context)
            throws JsonParseException {
        final JsonObject wrapper = (JsonObject) elem;
        final JsonElement typeName = wrapper.get("url");
        Class<? extends ContentSearch> actualClass = null;
        if (typeName.getAsString().startsWith("https://www.youtube.com/")) {
            actualClass = YoutubeContentSearch.class;
        } else if (typeName.getAsString().startsWith("https://bandcamp.com/tag/")) {
            actualClass = BandcampContentSearch.class;
        }
        return context.deserialize(elem, actualClass);
    }

}