package ch.hsr.markovshield.models;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import java.util.Date;

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
}
