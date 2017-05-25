package ch.hsr.markovshield.flink;

import ch.hsr.markovshield.models.MarkovRating;
import ch.hsr.markovshield.models.ValidatedClickStream;

public class ValidatedClickStreamHelper {

    private ValidatedClickStreamHelper() {

    }

    public static ValidatedClickStream foldValidationClickStream(ValidatedClickStream acc,
                                                                 ValidatedClickStream newClickStream) {
        if (acc == null) {
            return newClickStream;
        }
        if (newClickStream == null) {
            return acc;
        }
        MarkovRating newRating = newClickStream.getClickStreamValidation().getRating();
        MarkovRating accumulatedRating = acc.getClickStreamValidation().getRating();
        if (newRating == accumulatedRating) {
            return newClickStream;
        }
        if (newRating.ordinal() < accumulatedRating.ordinal()) {
            return new ValidatedClickStream(newClickStream.getUserName(),
                newClickStream.getSessionUUID(),
                newClickStream.getClicks(),
                acc.getClickStreamValidation());
        } else {
            return newClickStream;
        }
    }
}
