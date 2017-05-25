package ch.hsr.markovshield.kafkastream.service;

import ch.hsr.markovshield.models.UserModel;
import java.util.List;

public interface UserModelService {

    List<UserModel> getAllUserModels();

    UserModel getUserModel(String user);
}
