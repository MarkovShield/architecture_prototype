package ch.hsr.markovshield.models;

import ch.hsr.markovshield.ml_models.ClickStreamModel;
import java.io.Serializable;
import java.util.List;

public interface UserModelFactory extends Serializable {

    UserModel trainUserModel(Iterable<ClickStream> clickStreams, String userId);
}
