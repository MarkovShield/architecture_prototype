package ch.hsr.markovshield.kafkastream.service;

import ch.hsr.markovshield.kafkastream.repository.KafkaStateRepository;
import ch.hsr.markovshield.kafkastream.streaming.MarkovClickStreamProcessing;
import ch.hsr.markovshield.models.UserModel;
import javax.inject.Inject;
import java.util.List;

/**
 * Created by maede on 24.05.2017.
 */
public class LocalUserModelService implements UserModelService {

    private final KafkaStateRepository kafkaStateRepository;

    @Inject
    public LocalUserModelService(KafkaStateRepository kafkaStateRepository) {
        this.kafkaStateRepository = kafkaStateRepository;
    }

    @Override
    public List<UserModel> getAllUserModels() {
        return kafkaStateRepository.getAllValues(MarkovClickStreamProcessing.MARKOV_USER_MODEL_STORE);

    }

    @Override
    public UserModel getUserModel(String user) {
        return kafkaStateRepository.getValue(user, MarkovClickStreamProcessing.MARKOV_USER_MODEL_STORE);
    }
}
