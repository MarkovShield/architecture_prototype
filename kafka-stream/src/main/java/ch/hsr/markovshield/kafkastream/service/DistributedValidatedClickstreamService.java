package ch.hsr.markovshield.kafkastream.service;

import ch.hsr.markovshield.kafkastream.repository.DistributedKafkaStateRepository;
import ch.hsr.markovshield.kafkastream.streaming.MarkovClickStreamProcessing;
import ch.hsr.markovshield.models.ValidatedClickStream;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DistributedValidatedClickstreamService implements ValidatedClickstreamService {

    private final DistributedKafkaStateRepository kafkaRepository;
    private final SessionService localSessionService;

    public DistributedValidatedClickstreamService(DistributedKafkaStateRepository kafkaRepository,
                                                  SessionService localSessionService) {
        this.kafkaRepository = kafkaRepository;
        this.localSessionService = localSessionService;
    }

    @Override
    public List<ValidatedClickStream> getLocalValidatedClickstreams() {
        return kafkaRepository.getAllLocalValues(MarkovClickStreamProcessing.MARKOV_VALIDATED_CLICKSTREAMS_STORE);

    }

    @Override
    public List<ValidatedClickStream> getAllValidatedClickstreams() {
        return kafkaRepository.getAllValues(MarkovClickStreamProcessing.MARKOV_VALIDATED_CLICKSTREAMS_STORE,
            "/local/validatedclickstreams");
    }

    @Override
    public List<ValidatedClickStream> getValidatedClickstreamsByUser(String user) {
        Stream<ValidatedClickStream> validatedClickStreamStream = localSessionService.getSessionByUser(user)
            .stream()
            .map(session -> getValidatedClickstream(
                session.getSessionUUID()));
        return validatedClickStreamStream.collect(Collectors.toList());
    }

    @Override
    public ValidatedClickStream getValidatedClickstream(String uuid) {
        return kafkaRepository.getValue(uuid,
            MarkovClickStreamProcessing.MARKOV_VALIDATED_CLICKSTREAMS_STORE,
            "validatedclickstream/" + uuid,
            ValidatedClickStream.class);
    }
}
