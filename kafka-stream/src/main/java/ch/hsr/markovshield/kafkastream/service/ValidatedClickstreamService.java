package ch.hsr.markovshield.kafkastream.service;

import ch.hsr.markovshield.models.ValidatedClickStream;
import java.util.List;

/**
 * Created by maede on 24.05.2017.
 */
public interface ValidatedClickstreamService {

    List<ValidatedClickStream> getLocalValidatedClickstreams();

    List<ValidatedClickStream> getAllValidatedClickstreams();

    ValidatedClickStream getValidatedClickstream(String uuid);
}
