package ch.hsr.markovshield.models;

import java.time.Instant;

/**
 * Created by maede on 03.04.2017.
 */
public class UserModel {
    private String user;
    private TransitionModel transitionModel;
    private FrequencyModel frequencyModel;
    public UserModel() {
    }

    public UserModel(String user, TransitionModel transitionModel, FrequencyModel frequencyModel) {
        this.user = user;
        this.transitionModel = transitionModel;
        this.frequencyModel = frequencyModel;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public TransitionModel getTransitionModel() {
        return transitionModel;
    }

    public void setTransitionModel(TransitionModel transitionModel) {
        this.transitionModel = transitionModel;
    }

    public FrequencyModel getFrequencyModel() {
        return frequencyModel;
    }

    public void setFrequencyModel(FrequencyModel frequencyModel) {
        this.frequencyModel = frequencyModel;
    }
    public Instant timeCreated() {
        if(this.frequencyModel.timeCreated().isBefore(this.transitionModel.timeCreated())){
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
