package ch.hsr.markovshield.kafkastream.repository;

import ch.hsr.markovshield.models.Session;
import java.util.List;

/**
 * Created by maede on 24.05.2017.
 */
public interface KafkaStateRepository {

    <T> List<T> getAllValues(String storeName);

    <T> T getValue(String key, String storeName);
}
