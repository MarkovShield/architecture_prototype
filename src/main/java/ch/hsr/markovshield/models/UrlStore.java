package ch.hsr.markovshield.models;


import com.google.common.collect.ImmutableSortedMap;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class UrlStore {

    private final Map<String, Integer> urlMapping;

    public UrlStore(Map<String, Integer> urlMapping) {
        this.urlMapping = Collections.unmodifiableMap(new HashMap<>(urlMapping));
    }

    public Map<String, Integer> getUrlMapping() {
        return Collections.unmodifiableMap(urlMapping);
    }

    public Optional<Integer> get(String url) {
        Integer integer = urlMapping.get(url);
        return Optional.ofNullable(integer);
    }
}
