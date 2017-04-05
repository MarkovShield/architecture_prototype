package ch.hsr.markovshield.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import java.util.Date;
import java.util.Random;


public class FrequencyModel {

    private final int frequencyValue;
    private final Date timeCreated;

    public FrequencyModel() {
        frequencyValue = new Random().nextInt(100);
        this.timeCreated = Date.from(Instant.now());
    }

    @JsonCreator
    public FrequencyModel(@JsonProperty ("frequencyValue") int frequencyValue, @JsonProperty ("timeCreated") Date timeCreated) {
        this.frequencyValue = frequencyValue;
        this.timeCreated = timeCreated;
    }

    public int getFrequencyValue() {
        return frequencyValue;
    }

    public Date getTimeCreated() {
        return this.timeCreated;
    }

    @Override
    public String toString() {
        return "FrequencyModel{" +
            "frequencyValue=" + frequencyValue +
            ", timeCreated=" + timeCreated +
            '}';
    }
}
