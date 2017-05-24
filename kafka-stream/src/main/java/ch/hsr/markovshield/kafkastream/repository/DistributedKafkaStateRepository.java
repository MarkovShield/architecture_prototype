package ch.hsr.markovshield.kafkastream.repository;

import java.util.List;

/**
 * Created by maede on 24.05.2017.
 */
public interface DistributedKafkaStateRepository {

    <T> List<T> getAllLocalValues(String storeName);

    <T> List<T> getLocalValue(String key, String storeName);

    <T> List<T> getAllValues(String storeName, String path);

    <T> T getValue(String key, String storeName, String path, Class<T> classType);
}
