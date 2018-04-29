package lol.lolpany.postamatik;

import com.google.gson.annotations.SerializedName;

public enum ContentLength {
    @SerializedName("short")
    SHORT,
    @SerializedName("medium")
    MEDIUM,
    @SerializedName("long")
    LONG;

    public static ContentLength fromMinutes(long minutes) {
        if (minutes < 15) {
            return SHORT;
        } else if (minutes < 45) {
            return MEDIUM;
        } else {
            return LONG;
        }
    }
}
