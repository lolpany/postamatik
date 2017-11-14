package lol.lolpany.postamatik;

import com.google.gson.*;

import java.lang.reflect.Type;

final class ContentSearchDeserializer implements JsonDeserializer<ContentSearch> {

    public ContentSearch deserialize(JsonElement elem, Type interfaceType, JsonDeserializationContext context)
            throws JsonParseException {
        final JsonObject wrapper = (JsonObject) elem;
        final JsonElement typeName = wrapper.get("url");
        Class<? extends ContentSearch> actualClass = null;
        if (typeName.getAsString().startsWith("https://www.youtube.com/")) {
            actualClass = YoutubeContentSearch.class;
        }
        return context.deserialize(elem, actualClass);
    }

}