package ch.hsr.markovshield.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public class ValidationClickStream extends ClickStream {

    private UserModel userModel;
    private Date kafkaLeftDate;

    @JsonCreator
    public ValidationClickStream(@JsonProperty ("userName") String userName,
                                 @JsonProperty ("sessionUUID") String sessionUUID,
                                 @JsonProperty ("clicks") List<Click> clicks,
                                 @JsonProperty ("userModel") UserModel userModel,
                                 @JsonProperty ("kafkaLeftDate") Date kafkaLeftDate) {
        super(userName, sessionUUID, clicks);
        this.userModel = userModel;
        this.kafkaLeftDate = kafkaLeftDate;
    }

    public ValidationClickStream(String userName,
                                 String sessionUUID,
                                 List<Click> clicks,
                                 UserModel userModel) {
        super(userName, sessionUUID, clicks);
        this.userModel = userModel;
    }

    public static ValidationClickStream fromClickStream(ClickStream clickStream) {
        ValidationClickStream validationClickStream = new ValidationClickStream(clickStream.getUserName(),
            clickStream.getSessionUUID(),
            clickStream.getClicks(),
            null,
            null);
        return validationClickStream;
    }

    public UserModel getUserModel() {
        return userModel;
    }

    public void setUserModel(UserModel userModel) {
        this.userModel = userModel;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        ValidationClickStream that = (ValidationClickStream) o;
        return Objects.equals(userModel, that.userModel);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), userModel);
    }

    @Override
    public String toString() {
        return "ValidationClickStream{" +
            "userModel=" + userModel +
            ", kafkaLeftDate=" + kafkaLeftDate +
            '}';
    }

    public Date getKafkaLeftDate() {
        return kafkaLeftDate;
    }

    public void setKafkaLeftDate(Date kafkaLeftDate) {
        this.kafkaLeftDate = kafkaLeftDate;
    }
}
