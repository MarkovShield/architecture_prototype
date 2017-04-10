package ch.hsr.markovshield.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;

/**
 * Created by maede on 08.04.2017.
 */
public class UrlConfiguration {

    private final String url;
    private final UrlRating rating;

    public UrlConfiguration(@JsonProperty ("url") String url,
                            @JsonProperty ("rating") UrlRating rating) {
        this.url = url;
        this.rating = rating;
    }

    public String getUrl() {
        return url;
    }

    public UrlRating getRating() {
        return rating;
    }

    @Override
    public String toString() {
        return "UrlConfiguration{" +
            "url='" + url + '\'' +
            ", rating=" + rating +
            '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        UrlConfiguration that = (UrlConfiguration) o;
        return Objects.equals(url, that.url) &&
            Objects.equals(rating, that.rating);
    }

    @Override
    public int hashCode() {
        return Objects.hash(url, rating);
    }
}
