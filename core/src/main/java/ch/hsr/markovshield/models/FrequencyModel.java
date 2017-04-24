package ch.hsr.markovshield.models;

import ch.hsr.markovshield.ml.FrequencyMatrix;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import java.util.Date;
import java.util.Objects;
import java.util.Optional;

@JsonIgnoreProperties (ignoreUnknown = true)
public class FrequencyModel {

    private final FrequencyMatrix frequencyMatrix;
    private final UrlStore urlStore;
    private final Date timeCreated;

    public FrequencyModel(FrequencyMatrix frequencyMatrix, UrlStore urlStore) {
        this.frequencyMatrix = frequencyMatrix;
        this.urlStore = urlStore;
        this.timeCreated = Date.from(Instant.now());
    }

    @JsonCreator
    public FrequencyModel(@JsonProperty ("frequencyMatrix") FrequencyMatrix frequencyMatrix,
                          @JsonProperty ("urlStore") UrlStore urlStore,
                          @JsonProperty ("timeCreated") Date timeCreated) {
        this.frequencyMatrix = frequencyMatrix;
        this.urlStore = urlStore;
        this.timeCreated = timeCreated;
    }

    @Override
    public String toString() {
        return "FrequencyModel{" +
            "frequencyMatrix=" + frequencyMatrix +
            ", urlStore=" + urlStore +
            ", timeCreated=" + timeCreated +
            '}';
    }

    public Date getTimeCreated() {
        return this.timeCreated;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        FrequencyModel that = (FrequencyModel) o;
        return Objects.equals(frequencyMatrix, that.frequencyMatrix) &&
            Objects.equals(urlStore, that.urlStore) &&
            Objects.equals(timeCreated, that.timeCreated);
    }

    @Override
    public int hashCode() {
        return Objects.hash(frequencyMatrix, urlStore, timeCreated);
    }

    public double getFrequencyLowerBound(Click currentClick) {
        return getFrequencyLowerBound(currentClick.getUrl());
    }

    private Optional<Integer> getIndexByUrl(String url) {
        return this.urlStore.get(url);
    }

    public double getFrequencyLowerBound(String currentUrl) {
        Optional<Integer> sourceIndex = getIndexByUrl(currentUrl);
        double lowerBound;
        if (sourceIndex.isPresent()) {
            lowerBound = frequencyMatrix.get(sourceIndex.get(), 0);
        } else {
            lowerBound = 0;
        }
        return lowerBound;
    }

    public double getFrequencyUpperBound(Click currentClick) {
        return getFrequencyLowerBound(currentClick.getUrl());
    }

    public double getFrequencyUpperBound(String currentUrl) {
        Optional<Integer> sourceIndex = getIndexByUrl(currentUrl);
        double lowerBound;
        if (sourceIndex.isPresent()) {
            lowerBound = frequencyMatrix.get(sourceIndex.get(), 1);
        } else {
            lowerBound = 0;
        }
        return lowerBound;
    }
}
