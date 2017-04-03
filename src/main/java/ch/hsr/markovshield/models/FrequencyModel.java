package ch.hsr.markovshield.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import java.util.Random;


public class FrequencyModel {

    private final int frequencyValue;

    public FrequencyModel() {
        frequencyValue = new Random().nextInt(100);
    }

    @JsonCreator
    public FrequencyModel(@JsonProperty ("frequencyValue") int frequencyValue) {
        this.frequencyValue = frequencyValue;
    }

    public int getFrequencyValue() {
        return frequencyValue;
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
