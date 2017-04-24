package ch.hsr.markovshield.models;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonAutoDetect (fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class ValidatedClickStream extends ClickStream {

    private ClickStreamValidation clickStreamValidation;

    public ValidatedClickStream(String userName,
                                String sessionUUID,
                                List<Click> clicks) {
        super(userName, sessionUUID, clicks);
        this.clickStreamValidation = new ClickStreamValidation(userName,
            sessionUUID,
            lastClick().map(Click::getClickUUID).orElse(""),
            0,
            MarkovRating.UNEVALUDATED);
    }

    @JsonCreator
    public ValidatedClickStream(@JsonProperty ("userName") String userName,
                                @JsonProperty ("sessionUUID") String sessionUUID,
                                @JsonProperty ("clicks") List<Click> clicks,
                                @JsonProperty ("clickStreamValidation") ClickStreamValidation clickStreamValidation) {
        super(userName, sessionUUID, clicks);
        this.clickStreamValidation = clickStreamValidation;
    }

    public ClickStreamValidation getClickStreamValidation() {

        return clickStreamValidation;

    }
}
