package ch.hsr.markovshield.kafkastream.streaming;

import ch.hsr.markovshield.kafkastream.repository.DistributedKafkaStateRepository;
import ch.hsr.markovshield.kafkastream.repository.KafkaStateRepository;
import ch.hsr.markovshield.kafkastream.repository.LocalKafkaStateRepository;
import ch.hsr.markovshield.kafkastream.repository.SimpleDistributedKafkaStateRepository;
import ch.hsr.markovshield.kafkastream.service.DistributedMetadataService;
import ch.hsr.markovshield.kafkastream.service.DistributedValidatedClickstreamService;
import ch.hsr.markovshield.kafkastream.service.LocalSessionService;
import ch.hsr.markovshield.kafkastream.service.LocalUserModelService;
import ch.hsr.markovshield.kafkastream.service.MetadataService;
import ch.hsr.markovshield.kafkastream.service.SessionService;
import ch.hsr.markovshield.kafkastream.service.UserModelService;
import ch.hsr.markovshield.kafkastream.service.ValidatedClickstreamService;
import ch.hsr.markovshield.kafkastream.web.MarkovRestService;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.kstream.KStreamBuilder;
import org.apache.kafka.streams.state.HostInfo;
import java.util.Properties;

/**
 * Created by maede on 19.04.2017.
 */
public class StreamingApplication {


    private final Properties streamConfiguration;
    private final KStreamBuilder builder;

    public StreamingApplication(Properties streamConfiguration, KStreamBuilder builder) {
        this.streamConfiguration = streamConfiguration;
        this.builder = builder;
    }

    public void startStreamingApp(HostInfo restEndpoint) throws Exception {
        final KafkaStreams streams = new KafkaStreams(builder, streamConfiguration);
        streams.cleanUp();
        streams.start();
        System.out.println("REST endpoint at http://" + restEndpoint.host() + ":" + restEndpoint.port());
        final MarkovRestService restService = startRestProxy(streams, restEndpoint);

        // Add shutdown hook to respond to SIGTERM and gracefully close Kafka Streams
        addShutdown(streams, restService);
    }

    private static void addShutdown(KafkaStreams streams, MarkovRestService restService) {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                restService.stop();
                streams.close();
            } catch (Exception e) {
                // ignored
            }
        }));
    }

    static MarkovRestService startRestProxy(final KafkaStreams streams, final HostInfo hostInfo)
        throws Exception {

        MetadataService metadataService = new DistributedMetadataService(streams);
        KafkaStateRepository localStateRepository = new LocalKafkaStateRepository(streams);
        SessionService sessionService = new LocalSessionService(localStateRepository);
        UserModelService userModelService = new LocalUserModelService(localStateRepository);
        DistributedKafkaStateRepository distributedStateRepository = new SimpleDistributedKafkaStateRepository(
            localStateRepository,
            metadataService,
            hostInfo,
            "markovShield");
        ValidatedClickstreamService validatedClickstreamService = new DistributedValidatedClickstreamService(
            distributedStateRepository, sessionService);
        final MarkovRestService
            interactiveQueriesRestService = new MarkovRestService(hostInfo, metadataService, sessionService,
            validatedClickstreamService,
            userModelService);
        interactiveQueriesRestService.start();
        return interactiveQueriesRestService;
    }
}
