package ch.hsr.markovshield.ml_models.builder;

import ch.hsr.markovshield.ml_models.ModelBuilder;
import ch.hsr.markovshield.ml_models.RandomModel;
import ch.hsr.markovshield.models.ClickStream;
import ch.hsr.markovshield.models.ClickStreamModel;

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
