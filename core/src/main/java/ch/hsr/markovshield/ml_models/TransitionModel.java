package ch.hsr.markovshield.ml_models;

import ch.hsr.markovshield.ml_models.builder.MarkovChainAnalysis;
import ch.hsr.markovshield.ml_models.data_helper.TransitionMatrix;
import ch.hsr.markovshield.ml_models.data_helper.UrlStore;
import ch.hsr.markovshield.models.Click;
import ch.hsr.markovshield.models.ClickStream;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@JsonAutoDetect (fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class TransitionModel implements ClickStreamModel {

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

    private UrlStore getUrlStore() {
        return this.urlStore;
    }

    private TransitionMatrix getTransitionMatrix() {
        return this.transitionMatrix;
    }

    public Date getTimeCreated() {
        return timeCreated;
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

    @Override
    public String toString() {
        return "TransitionModel{" +
            "transitionMatrix=" + transitionMatrix +
            ", urlStore=" + urlStore +
            ", timeCreated=" + timeCreated +
            '}';
    }

    @Override
    public double clickStreamScore(ClickStream clickStream) {
        double transitionScore = 0;
        List<Click> clicks = clickStream.getClicks();
        for (int i = 0; i < clicks.size(); i++) {
            double probabilityForClick;
            if (i == clicks.size() - 1) {
                probabilityForClick = getProbabilityForClick(clicks.get(i).getUrl(),
                    MarkovChainAnalysis.END_OF_CLICK_STREAM);
            } else {
                probabilityForClick = getProbabilityForClick(clicks.get(i), clicks.get(i + 1));
            }
            transitionScore += (1 - probabilityForClick) * clicks.get(i).getUrlRiskLevel();
        }
        return transitionScore;
    }

    public double getProbabilityForClick(Click currentClick, Click newClick) {
        return getProbabilityForClick(currentClick.getUrl(), newClick.getUrl());
    }

    public double getProbabilityForClick(String currentUrl, String newUrl) {
        Optional<Integer> sourceIndex = getIndexByUrl(currentUrl);
        Optional<Integer> targetIndex = getIndexByUrl(newUrl);
        double transistionProbability;
        if (sourceIndex.isPresent() && targetIndex.isPresent()) {
            transistionProbability = transitionMatrix.get(sourceIndex.get(), targetIndex.get());
        } else {
            transistionProbability = 0;
        }
        return transistionProbability;
    }

    private Optional<Integer> getIndexByUrl(String url) {
        return this.urlStore.get(url);
    }
}
