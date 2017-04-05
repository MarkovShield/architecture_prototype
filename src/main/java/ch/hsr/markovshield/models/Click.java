package ch.hsr.markovshield.models;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Date;

public class Click {

    private final String sessionId;
    private final String url;
    private final Date timeStamp;

    @JsonCreator
    public Click(@JsonProperty ("sessionId") String sessionId,
                 @JsonProperty ("url") String url,
                 @JsonProperty ("timeStamp") Date timeStamp) {
        this.sessionId = sessionId;
        this.url = url;
        this.timeStamp = timeStamp;
    }

    public String getSessionId() {
        return sessionId;
    }

    public String getUrl() {
        return url;
    }

    public Date getTimeStamp() {
        return timeStamp;
    }

    @Override
    public String toString() {
        return "Click{" +
            "sessionId='" + sessionId + '\'' +
            ", url='" + url + '\'' +
            ", timeStamp=" + timeStamp +
            '}';
    }
}
