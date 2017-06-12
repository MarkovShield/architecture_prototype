package ch.hsr.markovshield.kafkastream.interactive_query.repository;

import java.util.List;

public interface KafkaStateRepository {

    <T> List<T> getAllValues(String storeName);

    <T> T getValue(String key, String storeName);
}
