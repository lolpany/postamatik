package lol.lolpany.friendify;

import com.google.gson.*;
import lol.lolpany.Location;
import lol.lolpany.friendify.linkedin.LinkedInLocation;

import java.lang.reflect.Type;

public class LocationDeserializer implements JsonDeserializer<Location> {

    public Location deserialize(JsonElement elem, Type interfaceType, JsonDeserializationContext context)
            throws JsonParseException {
        final JsonObject wrapper = (JsonObject) elem;
        final JsonElement typeName = wrapper.get("url");
        Class<? extends Location> actualClass = null;
        if (typeName.getAsString().startsWith("https://www.linkedin.com/")) {
            actualClass = LinkedInLocation.class;
        }
        return context.deserialize(elem, actualClass);
    }
}
