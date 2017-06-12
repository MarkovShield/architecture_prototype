package ch.hsr.markovshield.kafkastream.interactive_query.repository;

import java.util.List;

public interface DistributedKafkaStateRepository {

    <T> List<T> getAllLocalValues(String storeName);

    <T> List<T> getLocalValue(String key, String storeName);

    <T> List<T> getAllValues(String storeName, String path);

    <T> T getValue(String key, String storeName, String path, Class<T> classType);
}
