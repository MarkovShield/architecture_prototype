package ch.hsr.markovshield.kafkastream.service;

import ch.hsr.markovshield.kafkastream.repository.DistributedKafkaStateRepository;
import ch.hsr.markovshield.kafkastream.streaming.MarkovClickStreamProcessing;
import ch.hsr.markovshield.models.ValidatedClickStream;
import java.util.List;

/**
 * Created by maede on 24.05.2017.
 */
public class DistributedValidatedClickstreamService implements ValidatedClickstreamService {

    private final DistributedKafkaStateRepository kafkaRepository;

    public DistributedValidatedClickstreamService(DistributedKafkaStateRepository kafkaRepository) {
        this.kafkaRepository = kafkaRepository;
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
    public ValidatedClickStream getValidatedClickstream(String uuid) {
        return kafkaRepository.getValue(uuid,
            MarkovClickStreamProcessing.MARKOV_VALIDATED_CLICKSTREAMS_STORE,
            "validatedclickstream/" + uuid,
            ValidatedClickStream.class);
    }
}
