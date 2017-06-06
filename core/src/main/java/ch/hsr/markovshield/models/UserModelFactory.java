package ch.hsr.markovshield.models;

import java.io.Serializable;

public interface UserModelFactory extends Serializable {

    UserModel trainUserModel(Iterable<ClickStream> clickStreams, String userId);
}
