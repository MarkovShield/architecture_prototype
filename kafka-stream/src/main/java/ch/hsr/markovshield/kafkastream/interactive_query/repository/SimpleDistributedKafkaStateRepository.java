package ch.hsr.markovshield.kafkastream.interactive_query.repository;

import ch.hsr.markovshield.kafkastream.interactive_query.models.HostStoreInfo;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.streams.state.HostInfo;
import org.glassfish.jersey.jackson.JacksonFeature;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.List;

public class SimpleDistributedKafkaStateRepository implements DistributedKafkaStateRepository {

    private final KafkaStateRepository localKafkaStateRepository;
    private final MetadataRepository metadataRepository;
    private final HostInfo hostInfo;
    private final Client client;
    private final String lookupRoot;


    public SimpleDistributedKafkaStateRepository(KafkaStateRepository localKafkaStateRepository,
                                                 MetadataRepository metadataRepository,
                                                 HostInfo hostInfo,
                                                 String lookupRoot) {
        this.localKafkaStateRepository = localKafkaStateRepository;
        this.metadataRepository = metadataRepository;
        this.hostInfo = hostInfo;
        this.lookupRoot = lookupRoot;
        client = ClientBuilder.newBuilder().register(JacksonFeature.class).build();
    }

    @Override
    public <T> List<T> getAllLocalValues(String storeName) {
        return localKafkaStateRepository.getAllValues(storeName);
    }

    @Override
    public <T> List<T> getLocalValue(String key, String storeName) {
        return localKafkaStateRepository.getValue(key, storeName);
    }

    @Override
    public <T> List<T> getAllValues(String storeName, String path) {
        List<T> allValuesFromOtherStores = getAllValuesFromOtherStores(storeName,
            path);
        allValuesFromOtherStores.addAll(localKafkaStateRepository.getAllValues(storeName));
        return allValuesFromOtherStores;
    }

    private <T> List<T> getAllValuesFromOtherStores(String markovUserModelStore, String path) {
        List<HostStoreInfo> hostStoreInfos = metadataRepository.streamsMetadata(markovUserModelStore);
        List<T> allModels = new ArrayList<>();
        for (HostStoreInfo info : hostStoreInfos
            ) {
            if (!thisHost(info)) {
                GenericType<List<T>> genericType = new GenericType<List<T>>() {
                };
                List<T> list = fetchValueFromOtherHost(info, path, genericType);
                allModels.addAll(list);
            }
        }
        return allModels;
    }

    private boolean thisHost(final HostStoreInfo host) {
        return host.getHost().equals(hostInfo.host()) &&
            host.getPort() == hostInfo.port();
    }

    public <T> T fetchValueFromOtherHost(final HostStoreInfo host, final String path, GenericType<T> classType) {
        String formattedUrl = String.format("http://%s:%d/%s/%s", host.getHost(), host.getPort(), lookupRoot, path);
        System.out.println(formattedUrl);
        return client.target(formattedUrl)
            .request(MediaType.APPLICATION_JSON_TYPE)
            .get(classType);
    }

    @Override
    public <T> T getValue(String key, String storeName, String path, Class<T> classType) {
        final HostStoreInfo
            host =
            metadataRepository.streamsMetadataForStoreAndKey(storeName, key, new
                StringSerializer());

        if (!thisHost(host)) {
            return fetchValueFromOtherHost(host, path, new GenericType<>(classType));
        }

        return localKafkaStateRepository.getValue(key, storeName);
    }
}
