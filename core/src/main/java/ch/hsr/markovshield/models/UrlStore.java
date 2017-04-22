package ch.hsr.markovshield.models;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class UrlStore {

    private final Map<String, Integer> urlMapping;

    @JsonCreator
    public UrlStore(@JsonProperty ("urlMapping") Map<String, Integer> urlMapping) {
        this.urlMapping = new HashMap<>(urlMapping);
    }

    public Map<String, Integer> getUrlMapping() {
        return Collections.unmodifiableMap(urlMapping);
    }

    public Optional<Integer> get(String url) {
        Integer integer = urlMapping.get(url);
        return Optional.ofNullable(integer);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        UrlStore urlStore = (UrlStore) o;
        return Objects.equals(urlMapping, urlStore.urlMapping);
    }

    @Override
    public int hashCode() {
        return Objects.hash(urlMapping);
    }

    @Override
    public String toString() {
        return "UrlStore{" +
            "urlMapping=" + urlMapping +
            '}';
    }
}
