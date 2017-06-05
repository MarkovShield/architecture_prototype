package ch.hsr.markovshield.ml_models;

import ch.hsr.markovshield.models.ClickStream;
import ch.hsr.markovshield.models.ClickStreamModel;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import java.util.Date;
import java.util.Random;

@JsonAutoDetect (fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class RandomModel implements ClickStreamModel {

    private final Date timeCreated;
    private final int suspiciousShare;
    private final int fraudShare;

    public RandomModel(int suspiciousShare, int fraudShare) {
        this(Date.from(Instant.now()), suspiciousShare, fraudShare);
    }

    @JsonCreator
    public RandomModel(@JsonProperty ("timeCreated") Date timeCreated,
                       @JsonProperty ("suspiciousShare") int suspiciousShare,
                       @JsonProperty ("fraudShare") int fraudShare) {
        this.timeCreated = timeCreated;
        this.suspiciousShare = suspiciousShare;
        this.fraudShare = fraudShare;
    }

    public Date getTimeCreated() {
        return this.timeCreated;
    }


    @Override
    public double clickStreamScore(ClickStream clickStream) {
        int i = new Random().nextInt(100);
        if (i >= (100 - fraudShare)) {
            return 100;
        }
        if (i >= (100 - fraudShare - suspiciousShare)) {
            return 75;
        } else {
            return i;
        }
    }
}
