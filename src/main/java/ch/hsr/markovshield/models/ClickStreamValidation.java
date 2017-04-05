package ch.hsr.markovshield.models;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import java.util.Date;

public class ClickStreamValidation {

    private final String userName;
    private final String sessionId;
    private final int validationScore;
    private final MarkovRating rating;
    private final Date timeCreated;

    public ClickStreamValidation(String userName, String sessionId, int validationScore, MarkovRating rating) {
        this.userName = userName;
        this.sessionId = sessionId;
        this.validationScore = validationScore;
        this.rating = rating;
        timeCreated = Date.from(Instant.now());
    }

    @JsonCreator
    public ClickStreamValidation(@JsonProperty ("userName") String userName,
                                 @JsonProperty ("sessionId") String sessionId,
                                 @JsonProperty ("validationScore") int validationScore,
                                 @JsonProperty ("rating") MarkovRating rating,
                                 @JsonProperty ("timeCreated") Date timeCreated) {
        this.userName = userName;
        this.sessionId = sessionId;
        this.validationScore = validationScore;
        this.rating = rating;
        this.timeCreated = timeCreated;
    }

    public Date getTimeCreated() {
        return timeCreated;
    }

    public MarkovRating getRating() {
        return rating;
    }

    public String getSessionId() {
        return sessionId;
    }

    public int getValidationScore() {
        return validationScore;
    }

    public String getUserName() {
        return userName;
    }
}
