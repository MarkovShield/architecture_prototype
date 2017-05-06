package ch.hsr.markovshield.ml;

import ch.hsr.markovshield.models.ClickStream;
import ch.hsr.markovshield.models.FrequencyModel;


public interface FrequencyAnalysis {

    FrequencyModel train(Iterable<ClickStream> stream);
}
