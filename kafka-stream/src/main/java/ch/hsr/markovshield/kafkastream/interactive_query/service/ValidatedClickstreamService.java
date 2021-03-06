package ch.hsr.markovshield.kafkastream.interactive_query.service;

import ch.hsr.markovshield.models.ValidatedClickStream;
import java.util.List;

public interface ValidatedClickstreamService {

    List<ValidatedClickStream> getLocalValidatedClickstreams();

    List<ValidatedClickStream> getAllValidatedClickstreams();

    ValidatedClickStream getValidatedClickstream(String uuid);

    List<ValidatedClickStream> getValidatedClickstreamsByUser(String user);

    List<ValidatedClickStream> getValidatedClickstreamAfterTimeStamp(Long timestamp);

    List<ValidatedClickStream> getValidatedClickstreamBetweenTimeStamps(Long timestampFirst, Long timestampLast);

    List<ValidatedClickStream> getValidatedClickstreamBeforeTimeStamp(Long timestamp);
}
