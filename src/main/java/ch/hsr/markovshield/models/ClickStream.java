package ch.hsr.markovshield.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collections;
import java.util.Date;
import java.util.List;


public class ClickStream {

    private final String userName;
    private final String sessionId;
    private final List<Click> clicks;
    private final UserModel userModel;

    @JsonCreator
    public ClickStream(@JsonProperty ("userName") String userName,
                       @JsonProperty ("sessionId") String sessionId,
                       @JsonProperty ("clicks") List<Click> clicks,
                       @JsonProperty ("userModel") UserModel userModel) {
        this.userName = userName;
        this.sessionId = sessionId;
        this.clicks = clicks;
        this.userModel = userModel;
    }

    public String getUserName() {
        return userName;
    }

    public String getSessionId() {
        return sessionId;
    }

    public List<Click> getClicks() {
        return clicks;
    }

    public UserModel getUserModel() {
        return userModel;
    }

    public Date getTimeStampOfLastClick() {
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
        return "ClickStream{" +
            "userName='" + userName + '\'' +
            ", sessionId='" + sessionId + '\'' +
            ", clicks=" + clicks +
            ", userModel=" + userModel +
            '}';
    }
}
