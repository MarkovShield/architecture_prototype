package ch.hsr.markovshield.ml;

import ch.hsr.markovshield.models.ClickStream;
import ch.hsr.markovshield.models.ClickStreamModel;
import java.io.Serializable;

public interface ModelBuilder extends Serializable {

    ClickStreamModel train(Iterable<ClickStream> stream);

}
