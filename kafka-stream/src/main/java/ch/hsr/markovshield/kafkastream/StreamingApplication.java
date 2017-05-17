package ch.hsr.markovshield.kafkastream;

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
        final MarkovRestService
            interactiveQueriesRestService = new MarkovRestService(streams, hostInfo);
        interactiveQueriesRestService.start();
        return interactiveQueriesRestService;
    }
}
