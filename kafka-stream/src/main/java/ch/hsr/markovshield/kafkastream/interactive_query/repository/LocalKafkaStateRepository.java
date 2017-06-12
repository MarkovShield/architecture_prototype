package ch.hsr.markovshield.kafkastream.interactive_query.repository;

import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.state.KeyValueIterator;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import javax.ws.rs.NotFoundException;
import java.util.ArrayList;
import java.util.List;

public class LocalKafkaStateRepository implements KafkaStateRepository {

    private final KafkaStreams streams;

    public LocalKafkaStateRepository(KafkaStreams streams) {
        this.streams = streams;
    }

    public <T> List<T> getAllValues(final String storeName) {
        final ReadOnlyKeyValueStore<String, T> store =
            streams.store(storeName, QueryableStoreTypes.<String, T>keyValueStore());
        List<T> allValues = new ArrayList<>();
        KeyValueIterator<String, T> all = store.all();
        for (KeyValueIterator<String, T> it = all; it.hasNext(); ) {
            KeyValue<String, T> x = it.next();
            allValues.add(x.value);
        }
        return allValues;
    }

    public <T> T getValue(final String key,
                          final String storeName) {

        final ReadOnlyKeyValueStore<String, T> store =
            streams.store(storeName, QueryableStoreTypes.<String, T>keyValueStore());
        final T value = store.get(key);
        if (value == null) {
            throw new NotFoundException(String.format("Unable to find value in %s for key %s", storeName, key));
        }
        return value;
    }
}
