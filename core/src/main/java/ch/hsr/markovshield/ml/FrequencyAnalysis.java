package ch.hsr.markovshield.ml;

import ch.hsr.markovshield.models.ClickStream;
import ch.hsr.markovshield.models.ClickStreamModel;

public interface FrequencyAnalysis {

    ClickStreamModel train(Iterable<ClickStream> stream);
}
