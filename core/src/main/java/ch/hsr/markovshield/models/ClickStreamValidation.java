package ch.hsr.markovshield.models;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import java.util.Date;
import java.util.Objects;

public class ClickStreamValidation {

    private final String userName;
    private final String sessionUUID;
    private final String clickUUID;
    private final double validationScore;
    private final MarkovRating rating;
    private final Date timeCreated;

    public ClickStreamValidation(String userName, String sessionUUID, String clickUUID, double validationScore, MarkovRating rating) {
        this.userName = userName;
        this.sessionUUID = sessionUUID;
        this.clickUUID = clickUUID;
        this.validationScore = validationScore;
        this.rating = rating;
        timeCreated = Date.from(Instant.now());
    }

    @JsonCreator
    public ClickStreamValidation(@JsonProperty ("userName") String userName,
                                 @JsonProperty ("sessionUUID") String sessionUUID,
                                 @JsonProperty ("clickUUID") String clickUUID,
                                 @JsonProperty ("validationScore") int validationScore,
                                 @JsonProperty ("rating") MarkovRating rating,
                                 @JsonProperty ("timeCreated") Date timeCreated) {
        this.userName = userName;
        this.sessionUUID = sessionUUID;
        this.clickUUID = clickUUID;
        this.validationScore = validationScore;
        this.rating = rating;
        this.timeCreated = timeCreated;
    }

    public String getClickUUID() {
        return clickUUID;
    }

    public Date getTimeCreated() {
        return timeCreated;
    }

    public MarkovRating getRating() {
        return rating;
    }

    public String getSessionUUID() {
        return sessionUUID;
    }

    public double getValidationScore() {
        return validationScore;
    }

    public String getUserName() {
        return userName;
    }

    @Override
    public String toString() {
        return "ClickStreamValidation{" +
            "userName='" + userName + '\'' +
            ", sessionUUID='" + sessionUUID + '\'' +
            ", clickUUID='" + clickUUID + '\'' +
            ", validationScore=" + validationScore +
            ", rating=" + rating +
            ", timeCreated=" + timeCreated +
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
        ClickStreamValidation that = (ClickStreamValidation) o;
        return validationScore == that.validationScore &&
            Objects.equals(userName, that.userName) &&
            Objects.equals(sessionUUID, that.sessionUUID) &&
            Objects.equals(clickUUID, that.clickUUID) &&
            rating == that.rating &&
            Objects.equals(timeCreated, that.timeCreated);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userName, sessionUUID, clickUUID, validationScore, rating, timeCreated);
    }
}
