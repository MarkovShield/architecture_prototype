package ch.hsr.markovshield.ml_models;

import ch.hsr.markovshield.models.ClickStream;
import java.io.Serializable;

public interface ModelBuilder extends Serializable {

    ClickStreamModel train(Iterable<ClickStream> stream);

}
