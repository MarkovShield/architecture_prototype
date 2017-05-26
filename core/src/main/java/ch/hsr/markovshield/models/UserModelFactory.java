package ch.hsr.markovshield.models;

import java.util.List;

public interface UserModelFactory {

    List<ClickStreamModel> trainAllModels(Iterable<ClickStream> clickStreams);
}
