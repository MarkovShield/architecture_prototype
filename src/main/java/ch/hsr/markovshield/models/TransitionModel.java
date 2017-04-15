package ch.hsr.markovshield.models;

import ch.hsr.markovshield.ml.TransitionMatrix;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import java.util.Date;
import java.util.Objects;
import java.util.Optional;

@JsonAutoDetect (fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class TransitionModel {

    private final TransitionMatrix transitionMatrix;
    private final UrlStore urlStore;
    private final Date timeCreated;

    public TransitionModel(TransitionMatrix transitionMatrix, UrlStore urlStore) {
        this.transitionMatrix = transitionMatrix;
        this.urlStore = urlStore;
        timeCreated = Date.from(Instant.now());

    }

    @JsonCreator
    private TransitionModel(@JsonProperty ("transitionMatrix") TransitionMatrix transitionMatrix,
                           @JsonProperty ("urlStore") UrlStore urlStore,
                           @JsonProperty ("timeCreated") Date timeCreated) {
        this.transitionMatrix = transitionMatrix;
        this.urlStore = urlStore;
        this.timeCreated = timeCreated;
    }

    private UrlStore getUrlStore(){
        return this.urlStore;
    }
    private TransitionMatrix getTransitionMatrix(){
        return this.transitionMatrix;
    }
    public Date getTimeCreated() {
        return timeCreated;
    }

    private Optional<Integer> getIndexByUrl(String url) {
        return this.urlStore.get(url);
    }

    public double getProbabilityForClick(Click currentClick, Click newClick) {
        Optional<Integer> sourceIndex = getIndexByUrl(currentClick.getUrl());
        Optional<Integer> targetIndex = getIndexByUrl(newClick.getUrl());
        double transistionProbability;
        if (sourceIndex.isPresent() && targetIndex.isPresent()) {
            transistionProbability = transitionMatrix.get(sourceIndex.get(), targetIndex.get());
        } else {
            transistionProbability = 0;
        }
        return transistionProbability;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        TransitionModel that = (TransitionModel) o;
        return Objects.equals(transitionMatrix, that.transitionMatrix) &&
            Objects.equals(urlStore, that.urlStore) &&
            Objects.equals(timeCreated, that.timeCreated);
    }

    @Override
    public int hashCode() {
        return Objects.hash(transitionMatrix, urlStore, timeCreated);
    }
}
