package ch.hsr.markovshield.ml;

import ch.hsr.markovshield.models.ClickStream;
import ch.hsr.markovshield.models.ClickStreamModel;
import ch.hsr.markovshield.models.RandomModel;

public class RandomModelBuilder implements ModelBuilder {

    private final int suspiciousShare;
    private final int fraudShare;

    public RandomModelBuilder(int suspiciousShare, int fraudShare) {
        this.suspiciousShare = suspiciousShare;
        this.fraudShare = fraudShare;
    }

    @Override
    public ClickStreamModel train(Iterable<ClickStream> stream) {
        return new RandomModel(suspiciousShare, fraudShare);
    }
}
