package ch.hsr.markovshield.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Date;
import java.util.List;
import java.util.Optional;


public class ClickStream {

    private final String userName;
    private final String sessionUUID;
    private final List<Click> clicks;

    @JsonCreator
    public ClickStream(@JsonProperty ("userName") String userName,
                       @JsonProperty ("sessionUUID") String sessionUUID,
                       @JsonProperty ("clicks") List<Click> clicks) {
        this.userName = userName;
        this.sessionUUID = sessionUUID;
        this.clicks = clicks;
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

    public Date timeStampOfLastClick() {
        return clicks.stream().map(Click::getTimeStamp).max(Date::compareTo).orElse(new Date(0));
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
            ", sessionUUID='" + sessionUUID + '\'' +
            ", clicks=" + clicks +
            '}';
    }
}
