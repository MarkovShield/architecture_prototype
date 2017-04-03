package ch.hsr.markovshield.models;

import java.time.Instant;
import java.util.Random;

/**
 * Created by maede on 03.04.2017.
 */
public class FrequencyModel {
    private int frequencyValue;

    public FrequencyModel() {
        frequencyValue = new Random().nextInt(100);
    }

    public FrequencyModel(int frequencyValue) {
        this.frequencyValue = frequencyValue;
    }

    public int getFrequencyValue() {
        return frequencyValue;
    }

    public void setFrequencyValue(int frequencyValue) {
        this.frequencyValue = frequencyValue;
    }

    public Instant timeCreated() {
        return Instant.now();
    }

    @Override
    public String toString() {
        return "FrequencyModel{" +
                "frequencyValue=" + frequencyValue +
                '}';
    }
}
