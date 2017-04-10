package ch.hsr.markovshield.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;


public class ClickStream {

    private final String userName;
    private final String sessionId;
    private final List<Click> clicks;

    @JsonCreator
    public ClickStream(@JsonProperty ("userName") String userName,
                       @JsonProperty ("sessionId") String sessionId,
                       @JsonProperty ("clicks") List<Click> clicks) {
        this.userName = userName;
        this.sessionId = sessionId;
        this.clicks = clicks;
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

    public Optional<Click> lastClick() {
        if (clicks.size() == 0) {
            return Optional.empty();
        } else {
            return Optional.ofNullable(clicks.get(clicks.size() - 1));
        }
    }

    @Override
    public String toString() {
        return "ValidationClickStream{" +
            "userName='" + userName + '\'' +
            ", sessionId='" + sessionId + '\'' +
            ", clicks=" + clicks +
            '}';
    }
}
