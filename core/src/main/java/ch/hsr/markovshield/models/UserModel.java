package ch.hsr.markovshield.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Date;
import java.util.Objects;


public class UserModel {

    private final String userId;
    private final TransitionModel transitionModel;
    private final MatrixFrequencyModel frequencyModel;

    @JsonCreator
    public UserModel(
        @JsonProperty ("userId") String userId,
        @JsonProperty ("transitionModel") TransitionModel transitionModel,
        @JsonProperty ("frequencyModel") MatrixFrequencyModel frequencyModel) {
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


    public MatrixFrequencyModel getFrequencyModel() {
        return frequencyModel;
    }

    public Date timeCreated() {
        if (this.frequencyModel.getTimeCreated().before(this.transitionModel.getTimeCreated())) {
            return this.frequencyModel.getTimeCreated();
        }
        return this.transitionModel.getTimeCreated();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        UserModel userModel = (UserModel) o;
        return Objects.equals(userId, userModel.userId) &&
            Objects.equals(transitionModel, userModel.transitionModel) &&
            Objects.equals(frequencyModel, userModel.frequencyModel);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, transitionModel, frequencyModel);
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
