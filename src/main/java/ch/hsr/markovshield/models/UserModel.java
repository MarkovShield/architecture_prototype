package ch.hsr.markovshield.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import java.util.Date;


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

    public Date timeCreated() {
        if (this.frequencyModel.getTimeCreated().before(this.transitionModel.getTimeCreated())) {
            return this.frequencyModel.getTimeCreated();
        }
        return this.transitionModel.getTimeCreated();
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
