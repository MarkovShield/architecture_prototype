package ch.hsr.markovshield.kafkastream.service;

import ch.hsr.markovshield.kafkastream.repository.KafkaStateRepository;
import ch.hsr.markovshield.kafkastream.streaming.MarkovClickStreamProcessing;
import ch.hsr.markovshield.models.Session;
import javax.inject.Inject;
import java.util.List;

/**
 * Created by maede on 24.05.2017.
 */
public class LocalSessionService implements SessionService {

    private final KafkaStateRepository kafkaStateRepository;

    @Inject
    public LocalSessionService(KafkaStateRepository kafkaStateRepository) {
        this.kafkaStateRepository = kafkaStateRepository;
    }

    @Override
    public List<Session> getAllSessions() {
        return kafkaStateRepository.getAllValues(MarkovClickStreamProcessing.MARKOV_LOGIN_STORE);
    }

    @Override
    public Session getSession(String sessionId) {
        return kafkaStateRepository.getValue(sessionId,
            MarkovClickStreamProcessing.MARKOV_LOGIN_STORE);
    }
}
