package ch.hsr.markovshield.models;

import ch.hsr.markovshield.ml.TransistionMatrix;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;


public class TransitionModel {

    private final TransistionMatrix transistionMatrix;
    private final UrlStore urlStore;
    private final Date timeCreated;

    public TransitionModel(TransistionMatrix transistionMatrix, UrlStore urlStore) {
        this.transistionMatrix = transistionMatrix;
        this.urlStore = urlStore;
        timeCreated = Date.from(Instant.now());

    }

    public TransitionModel(@JsonProperty ("transitionMatrix") TransistionMatrix transistionMatrix,
                           @JsonProperty ("urlStore") UrlStore urlStore,
                           @JsonProperty ("timeCreated") Date timeCreated) {
        this.transistionMatrix = transistionMatrix;
        this.urlStore = urlStore;
        this.timeCreated = timeCreated;
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
            transistionProbability = transistionMatrix.get(sourceIndex.get(), targetIndex.get());
        } else {
            transistionProbability = 0;
        }
        return transistionProbability;
    }
}
