package ch.hsr.markovshield.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;


public class UserModel {

    private final String user;
    private final TransitionModel transitionModel;
    private final FrequencyModel frequencyModel;

    @JsonCreator
    public UserModel(
        @JsonProperty ("user") String user,
        @JsonProperty ("transitionModel") TransitionModel transitionModel,
        @JsonProperty ("frequencyModel") FrequencyModel frequencyModel) {
        this.user = user;
        this.transitionModel = transitionModel;
        this.frequencyModel = frequencyModel;
    }


    public String getUser() {
        return user;
    }


    public TransitionModel getTransitionModel() {
        return transitionModel;
    }


    public FrequencyModel getFrequencyModel() {
        return frequencyModel;
    }

    public Instant timeCreated() {
        if (this.frequencyModel.timeCreated().isBefore(this.transitionModel.timeCreated())) {
            return this.frequencyModel.timeCreated();
        }
        return this.transitionModel.timeCreated();
    }

    @Override
    public String toString() {
        return "UserModel{" +
            "user='" + user + '\'' +
            ", transitionModel=" + transitionModel +
            ", frequencyModel=" + frequencyModel +
            '}';
    }
}
