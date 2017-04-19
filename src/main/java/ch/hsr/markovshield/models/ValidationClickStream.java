package ch.hsr.markovshield.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collections;
import java.util.Date;
import java.util.List;


public class ValidationClickStream {

    private final String userName;
    private final String sessionUUID;
    private final List<Click> clicks;
    private UserModel userModel;

    public void setUserModel(UserModel userModel) {
        this.userModel = userModel;
    }


    public static ValidationClickStream fromClickStream(ClickStream clickStream) {
        ValidationClickStream validationClickStream = new ValidationClickStream(clickStream.getUserName(),
            clickStream.getSessionUUID(),
            clickStream.getClicks(),
            null);
        return validationClickStream;
    }

    @JsonCreator
    public ValidationClickStream(@JsonProperty ("userName") String userName,
                                 @JsonProperty ("sessionUUID") String sessionUUID,
                                 @JsonProperty ("clicks") List<Click> clicks,
                                 @JsonProperty ("userModel") UserModel userModel) {
        this.userName = userName;
        this.sessionUUID = sessionUUID;
        this.clicks = clicks;
        this.userModel = userModel;
    }

    public String getUserName() {
        return userName;
    }

    public String getSessionUUID() {
        return sessionUUID;
    }

    public List<Click> getClicks() {
        return clicks;
    }

    public UserModel getUserModel() {
        return userModel;
    }

    public Date timeStampOfLastClick() {
        return Collections.max(clicks, (Click o1, Click o2) -> {
            if (o1.getTimeStamp().equals(o2.getTimeStamp())) {
                return 0;
            }
            if (o1.getTimeStamp().before(o2.getTimeStamp())) {
                return -1;
            } else {
                return 1;
            }
        }).getTimeStamp();
    }

    @Override
    public String toString() {
        return "ValidationClickStream{" +
            "userName='" + userName + '\'' +
            ", sessionUUID='" + sessionUUID + '\'' +
            ", clicks=" + clicks +
            ", userModel=" + userModel +
            '}';
    }

}