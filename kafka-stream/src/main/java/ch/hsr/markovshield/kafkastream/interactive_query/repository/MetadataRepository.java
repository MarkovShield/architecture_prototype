package ch.hsr.markovshield.kafkastream.interactive_query.repository;

import ch.hsr.markovshield.kafkastream.interactive_query.models.HostStoreInfo;
import org.apache.kafka.common.serialization.Serializer;
import java.util.List;

public interface MetadataRepository {

    List<HostStoreInfo> streamsMetadata();

    List<HostStoreInfo> streamsMetadata(final String storeName);

    List<HostStoreInfo> streamsMetadataForStore(final String store);

    public <K> HostStoreInfo streamsMetadataForStoreAndKey(final String store,
                                                           final K key,
                                                           final Serializer<K> serializer);
}
