package ch.hsr.markovshield.models;

import java.io.Serializable;
import java.util.List;

public interface UserModelFactory extends Serializable{

    List<ClickStreamModel> trainAllModels(Iterable<ClickStream> clickStreams);
}
