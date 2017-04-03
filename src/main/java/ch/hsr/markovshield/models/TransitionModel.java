package ch.hsr.markovshield.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import java.util.Random;


public class TransitionModel {

    private final int transitionValue;


    public TransitionModel() {
        transitionValue = new Random().nextInt(100);
    }

    @JsonCreator
    public TransitionModel(@JsonProperty ("transitionValue") int transitionValue) {
        this.transitionValue = transitionValue;
    }

    public int getTransitionValue() {
        return transitionValue;
    }

    public Instant timeCreated() {
        return Instant.now();
    }

    @Override
    public String toString() {
        return "TransitionModel{" +
            "transitionValue=" + transitionValue +
            '}';
    }
}
