package ch.hsr.markovshield.kafkastream.service;

import ch.hsr.markovshield.models.ValidatedClickStream;
import java.util.List;

public interface ValidatedClickstreamService {

    List<ValidatedClickStream> getLocalValidatedClickstreams();

    List<ValidatedClickStream> getAllValidatedClickstreams();

    ValidatedClickStream getValidatedClickstream(String uuid);

    List<ValidatedClickStream> getValidatedClickstreamsByUser(String user);

    List<ValidatedClickStream> getValidatedClickstreamAfterTimeStamp(Long timestamp);
}
