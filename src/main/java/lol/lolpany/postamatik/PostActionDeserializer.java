package lol.lolpany.postamatik;

import com.google.gson.*;

import java.lang.reflect.Type;

final class PostActionDeserializer implements JsonDeserializer<PostAction> {

    public PostAction deserialize(JsonElement elem, Type interfaceType, JsonDeserializationContext context)
            throws JsonParseException {
        final JsonObject wrapper = (JsonObject) elem;
        final JsonElement typeName = wrapper.get("location").getAsJsonObject().get("url");
        Class<? extends PostAction> actualClass = null;
        if (typeName.getAsString().startsWith("https://www.youtube.com/")) {
            actualClass = YoutubePostAction.class;
        }
        return context.deserialize(elem, actualClass);
    }

}