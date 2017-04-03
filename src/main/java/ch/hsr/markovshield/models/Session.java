package ch.hsr.markovshield.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;


public class Session {

    private final String sessionId;
    private final String userName;

    @JsonCreator
    public Session(@JsonProperty ("sessionId") String sessionId, @JsonProperty ("userName") String userName) {
        this.sessionId = sessionId;
        this.userName = userName;
    }

    public String getSessionId() {
        return sessionId;
    }

    public String getUserName() {
        return userName;
    }

    @Override
    public String toString() {
        return "Session{" +
            "sessionId='" + sessionId + '\'' +
            ", userName='" + userName + '\'' +
            '}';
    }
}
