package ch.hsr.markovshield.models;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Click {

    private final String sessionId;
    private final String url;

    @JsonCreator
    public Click(@JsonProperty ("sessionId") String sessionId,
                 @JsonProperty ("url") String url) {
        this.sessionId = sessionId;
        this.url = url;
    }

    public String getSessionId() {
        return sessionId;
    }

    public String getUrl() {
        return url;
    }

    @Override
    public String toString() {
        return "Click{" +
            "sessionId='" + sessionId + '\'' +
            ", url='" + url + '\'' +
            '}';
    }
}
