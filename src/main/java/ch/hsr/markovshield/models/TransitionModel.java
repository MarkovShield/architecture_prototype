package ch.hsr.markovshield.models;

import java.time.Instant;
import java.util.Random;

/**
 * Created by maede on 03.04.2017.
 */
public class TransitionModel {
    private int transitionValue;

    public TransitionModel() {
        transitionValue = new Random().nextInt(100);
    }

    public void setTransitionValue(int transitionValue) {
        this.transitionValue = transitionValue;
    }

    public int getTransitionValue() {
        return transitionValue;
    }

    public TransitionModel(int transitionValue) {
        this.transitionValue = transitionValue;
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
