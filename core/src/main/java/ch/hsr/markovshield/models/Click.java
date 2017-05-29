package ch.hsr.markovshield.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Date;
import java.util.Objects;

@JsonIgnoreProperties (ignoreUnknown = true)
public class Click {

    private final String sessionUUID;
    private final String clickUUID;
    private final int urlRiskLevel;
    private final String url;
    private final Date timeStamp;
    private Date kafkaFirstProcessedDate;
    private final boolean validationRequired;

    @JsonCreator
    public Click(@JsonProperty ("sessionUUID") String sessionUUID,
                 @JsonProperty ("clickUUID") String clickUUID,
                 @JsonProperty ("url") String url,
                 @JsonProperty ("urlRiskLevel") int urlRiskLevel,
                 @JsonProperty ("timeStamp") Date timeStamp,
                 @JsonProperty ("validationRequired") boolean validationRequired,
                 @JsonProperty ("kafkaFirstProcessedDate") Date kafkaFirstProcessedDate) {
        this.sessionUUID = sessionUUID;
        this.clickUUID = clickUUID;
        this.url = url;
        this.urlRiskLevel = urlRiskLevel;
        this.timeStamp = timeStamp;
        this.validationRequired = validationRequired;
        this.kafkaFirstProcessedDate = kafkaFirstProcessedDate;
    }


    public Click(String sessionUUID,
                 String clickUUID,
                 String url,
                 int urlRiskLevel,
                 Date timeStamp,
                 boolean validationRequired) {
        this.sessionUUID = sessionUUID;
        this.clickUUID = clickUUID;
        this.url = url;
        this.urlRiskLevel = urlRiskLevel;
        this.timeStamp = timeStamp;
        this.validationRequired = validationRequired;
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

    public long getTimeDifference() {
        if (kafkaFirstProcessedDate != null && timeStamp != null) {

            return kafkaFirstProcessedDate.toInstant().toEpochMilli() - timeStamp.toInstant().toEpochMilli();
        }
        return 0;
    }

    public boolean isValidationRequired() {
        return validationRequired;
    }

    @Override
    public String toString() {
        return "Click{" +
            "sessionUUID='" + sessionUUID + '\'' +
            ", clickUUID='" + clickUUID + '\'' +
            ", urlRiskLevel=" + urlRiskLevel +
            ", url='" + url + '\'' +
            ", timeStamp=" + timeStamp +
            ", kafkaFirstProcessedDate=" + kafkaFirstProcessedDate +
            ", validationRequired=" + validationRequired +
            ", timedifference=" + getTimeDifference() +
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
        Click click = (Click) o;
        return urlRiskLevel == click.urlRiskLevel &&
            validationRequired == click.validationRequired &&
            Objects.equals(sessionUUID, click.sessionUUID) &&
            Objects.equals(clickUUID, click.clickUUID) &&
            Objects.equals(url, click.url) &&
            Objects.equals(timeStamp, click.timeStamp);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sessionUUID, clickUUID, urlRiskLevel, url, timeStamp, validationRequired);
    }

    public String getClickUUID() {
        return clickUUID;
    }

    public int getUrlRiskLevel() {
        return urlRiskLevel;
    }

    public Date getKafkaFirstProcessedDate() {
        return kafkaFirstProcessedDate;
    }

    public void setKafkaFirstProcessedDate(Date kafkaFirstProcessedDate) {
        this.kafkaFirstProcessedDate = kafkaFirstProcessedDate;
    }
}
