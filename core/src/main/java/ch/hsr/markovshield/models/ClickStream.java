package ch.hsr.markovshield.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class ClickStream {

    private final List<Click> clicks;
    private String userName;
    private String sessionUUID;

    @JsonCreator
    public ClickStream(@JsonProperty ("userName") String userName,
                       @JsonProperty ("sessionUUID") String sessionUUID,
                       @JsonProperty ("clicks") List<Click> clicks) {
        this.userName = userName;
        this.sessionUUID = sessionUUID;
        this.clicks = new ArrayList<>(clicks);
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getSessionUUID() {
        return sessionUUID;
    }

    public void setSessionUUID(String sessionUUID) {
        this.sessionUUID = sessionUUID;
    }

    public void addToClicks(Click click) {
        this.clicks.add(click);
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
        return "ClickStream{" +
            "userName='" + userName + '\'' +
            ", sessionUUID='" + sessionUUID + '\'' +
            ", clicks=" + clicks +
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
        ClickStream that = (ClickStream) o;
        return Objects.equals(userName, that.userName) &&
            Objects.equals(sessionUUID, that.sessionUUID) &&
            Objects.equals(clicks, that.clicks);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userName, sessionUUID, clicks);
    }

    public void addToClicks(Collection<Click> clicks) {
        this.clicks.addAll(clicks);
    }
}
