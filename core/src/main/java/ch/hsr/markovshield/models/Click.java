package ch.hsr.markovshield.models;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Date;

public class Click {

    private final String sessionUUID;
    private final String clickUUID;
    private final int urlRiskLevel;
    private final String url;
    private final Date timeStamp;

    @JsonCreator
    public Click(@JsonProperty ("sessionUUID") String sessionUUID,
                 @JsonProperty ("clickUUID") String clickUUID,
                 @JsonProperty ("url") String url,
                 @JsonProperty ("urlRiskLevel") int urlRiskLevel,
                 @JsonProperty ("timeStamp") Date timeStamp) {
        this.sessionUUID = sessionUUID;
        this.clickUUID = clickUUID;
        this.url = url;
        this.urlRiskLevel = urlRiskLevel;
        this.timeStamp = timeStamp;
    }

    public String getSessionUUID() {
        return sessionUUID;
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
            "sessionUUID='" + sessionUUID + '\'' +
            ", url='" + url + '\'' +
            ", timeStamp=" + timeStamp +
            '}';
    }

    public String getClickUUID() {
        return clickUUID;
    }

    public int getUrlRiskLevel() {
        return urlRiskLevel;
    }
}
