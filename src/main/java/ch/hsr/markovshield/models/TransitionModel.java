package ch.hsr.markovshield.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import java.util.Date;
import java.util.Random;


public class TransitionModel {

    private final int transitionValue;
    private final Date timeCreated;

    public TransitionModel() {
        transitionValue = new Random().nextInt(100);
        timeCreated = Date.from(Instant.now());
    }

    @JsonCreator
    public TransitionModel(@JsonProperty ("transitionValue") int transitionValue, @JsonProperty ("timeCreated") Date timeCreated) {
        this.transitionValue = transitionValue;
        this.timeCreated = timeCreated;
    }

    public int getTransitionValue() {
        return transitionValue;
    }

    public Date getTimeCreated() {
        return timeCreated;
    }

    @Override
    public String toString() {
        return "TransitionModel{" +
            "transitionValue=" + transitionValue +
            ", timeCreated=" + timeCreated +
            '}';
    }
}
