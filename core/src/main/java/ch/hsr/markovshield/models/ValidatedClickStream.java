package ch.hsr.markovshield.models;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Date;
import java.util.List;

@JsonAutoDetect (fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class ValidatedClickStream extends ClickStream {

    private Date creationDate;
    private ClickStreamValidation clickStreamValidation;

    public ValidatedClickStream(String userName,
                                String sessionUUID,
                                List<Click> clicks,
                                Date creationDate) {
        super(userName, sessionUUID, clicks);
        this.creationDate = creationDate;
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
                                @JsonProperty ("clickStreamValidation") ClickStreamValidation clickStreamValidation,
                                @JsonProperty ("creationDate") Date creationDate) {
        super(userName, sessionUUID, clicks);
        this.clickStreamValidation = clickStreamValidation;
        this.creationDate = creationDate;
    }

    public ClickStreamValidation getClickStreamValidation() {

        return clickStreamValidation;

    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }
}
