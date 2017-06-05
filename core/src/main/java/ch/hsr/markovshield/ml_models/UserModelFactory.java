package ch.hsr.markovshield.ml_models;

import ch.hsr.markovshield.models.ClickStream;
import java.io.Serializable;
import java.util.List;

public interface UserModelFactory extends Serializable {

    List<ClickStreamModel> trainAllModels(Iterable<ClickStream> clickStreams);
}
