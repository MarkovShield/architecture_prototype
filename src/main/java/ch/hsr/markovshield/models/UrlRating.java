package ch.hsr.markovshield.models;

import com.fasterxml.jackson.annotation.JsonProperty;

public class UrlRating extends IntegerValue {


    private UrlRating(@JsonProperty ("rating") int rating) {
        super(rating);
    }

    public static UrlRating fromInt(int value) {
        return new UrlRating(value);
    }

    public static UrlRating empty() {
        return new UrlRating(-1);
    }
}
