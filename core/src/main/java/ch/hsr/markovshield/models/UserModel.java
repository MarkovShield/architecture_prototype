package ch.hsr.markovshield.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public class UserModel {

    private final String userId;
    private final List<ClickStreamModel> clickStreamModels;

    @JsonCreator
    public UserModel(
        @JsonProperty ("userId") String userId,
        @JsonProperty ("clickStreamModels") List<ClickStreamModel> clickStreamModels) {
        this.userId = userId;
        this.clickStreamModels = new ArrayList<>(clickStreamModels);
    }

    public String getUserId() {
        return userId;
    }

    public List<ClickStreamModel> getClickStreamModels() {
        return clickStreamModels;
    }

    public Date timeCreated() {
        return this.clickStreamModels.stream()
            .map(ClickStreamModel::getTimeCreated)
            .max(Date::compareTo)
            .orElse(new Date());
    }

    @Override
    public String toString() {
        return "UserModel{" +
            "userId='" + userId + '\'' +
            ", clickStreamModels=" + clickStreamModels +
            '}';
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
            Objects.equals(clickStreamModels, userModel.clickStreamModels);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, clickStreamModels);
    }
}
