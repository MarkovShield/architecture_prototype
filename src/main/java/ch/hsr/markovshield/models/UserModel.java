package ch.hsr.markovshield.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;


public class UserModel {

    private final String userId;
    private final TransitionModel transitionModel;
    private final FrequencyModel frequencyModel;

    @JsonCreator
    public UserModel(
        @JsonProperty ("userId") String userId,
        @JsonProperty ("transitionModel") TransitionModel transitionModel,
        @JsonProperty ("frequencyModel") FrequencyModel frequencyModel) {
        this.userId = userId;
        this.transitionModel = transitionModel;
        this.frequencyModel = frequencyModel;
    }


    public String getUserId() {
        return userId;
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
            "userId='" + userId + '\'' +
            ", transitionModel=" + transitionModel +
            ", frequencyModel=" + frequencyModel +
            '}';
    }
}
