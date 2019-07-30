package lol.lolpany.postamatik;

import com.google.gson.*;
import lol.lolpany.Location;
import lol.lolpany.postamatik.pornhub.PornhubLocation;
import lol.lolpany.postamatik.youtube.YoutubeLocation;

import java.lang.reflect.Type;

public final class LocationDeserializer implements JsonDeserializer<Location> {

    public Location deserialize(JsonElement elem, Type interfaceType, JsonDeserializationContext context)
            throws JsonParseException {
        final JsonObject wrapper = (JsonObject) elem;
        final JsonElement typeName = wrapper.get("url");
        Class<? extends Location> actualClass = null;
        if (typeName.getAsString().startsWith("https://www.youtube.com/")) {
            actualClass = YoutubeLocation.class;
        } else if (typeName.getAsString().startsWith("https://www.pornhub.com/")) {
            actualClass = PornhubLocation.class;
        }
        return context.deserialize(elem, actualClass);
    }

}