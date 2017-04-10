package ch.hsr.markovshield.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.Objects;

public class UrlRating implements Serializable {

    private final int rating;

    public UrlRating(@JsonProperty ("rating") int rating) {
        this.rating = rating;
    }

    public int getRating() {
        return rating;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        UrlRating urlRating = (UrlRating) o;
        return rating == urlRating.rating;
    }

    @Override
    public int hashCode() {
        return Objects.hash(rating);
    }

    @Override
    public String toString() {
        return "UrlRating{" +
            "rating=" + rating +
            '}';
    }
}
