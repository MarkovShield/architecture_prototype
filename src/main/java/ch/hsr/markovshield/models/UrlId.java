package ch.hsr.markovshield.models;

import com.fasterxml.jackson.annotation.JsonProperty;

public class UrlId extends IntegerValue {


    private UrlId(@JsonProperty ("rating") int rating) {
        super(rating);
    }

    public static UrlId fromInt(int value) {
        return new UrlId(value);
    }

    public static UrlId empty() {
        return new UrlId(-1);
    }
}
