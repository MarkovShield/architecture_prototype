package ch.hsr.markovshield.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Session {

    private final String sessionUUID;
    private final String userName;

    @JsonCreator
    public Session(@JsonProperty ("sessionUUID") String sessionUUID, @JsonProperty ("userName") String userName) {
        this.sessionUUID = sessionUUID;
        this.userName = userName;
    }

    public String getSessionUUID() {
        return sessionUUID;
    }

    public String getUserName() {
        return userName;
    }

    @Override
    public String toString() {
        return "Session{" +
            "sessionUUID='" + sessionUUID + '\'' +
            ", userName='" + userName + '\'' +
            '}';
    }
}
