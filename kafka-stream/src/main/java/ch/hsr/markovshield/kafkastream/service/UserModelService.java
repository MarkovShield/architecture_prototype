package ch.hsr.markovshield.kafkastream.service;

import ch.hsr.markovshield.models.UserModel;
import java.util.List;

/**
 * Created by maede on 24.05.2017.
 */
public interface UserModelService {

    List<UserModel> getAllUserModels();

    UserModel getUserModel(String user);
}
