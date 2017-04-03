package ch.hsr.markovshield.models;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ClickStreamValidation {

    private final String sessionId;
    private final int validationScore;
    private final MarkovRating rating;

    @JsonCreator
    public ClickStreamValidation(@JsonProperty ("sessionId") String sessionId,
                                 @JsonProperty ("validationScore") int validationScore,
                                 @JsonProperty ("rating") MarkovRating rating) {
        this.sessionId = sessionId;
        this.validationScore = validationScore;
        this.rating = rating;
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

}
