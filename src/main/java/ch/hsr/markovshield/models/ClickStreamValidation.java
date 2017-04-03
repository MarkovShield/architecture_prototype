package ch.hsr.markovshield.models;

/**
 * Created by maede on 03.04.2017.
 */
public class ClickStreamValidation {
    private String session;
    private int validationScore;
    private MarkovRatings rating;

    public ClickStreamValidation() {
    }

    public ClickStreamValidation(String session, int validationScore, MarkovRatings rating) {
        this.session = session;
        this.validationScore = validationScore;
        this.rating = rating;
    }

    public MarkovRatings getRating() {
        return rating;
    }

    public void setRating(MarkovRatings rating) {
        this.rating = rating;
    }

    public String getSession() {
        return session;
    }

    public void setSession(String session) {
        this.session = session;
    }

    public int getValidationScore() {
        return validationScore;
    }

    public void setValidationScore(int validationScore) {
        this.validationScore = validationScore;
    }


}
