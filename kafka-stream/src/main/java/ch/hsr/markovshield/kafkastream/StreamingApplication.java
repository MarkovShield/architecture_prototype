package ch.hsr.markovshield.kafkastream;

import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.kstream.KStreamBuilder;
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

    public void startStreamingApp() {
        final KafkaStreams streams = new KafkaStreams(builder, streamConfiguration);
        streams.cleanUp();
        streams.start();
        // Add shutdown hook to respond to SIGTERM and gracefully close Kafka Streams
        addShutdown(streams);
    }

    private static void addShutdown(KafkaStreams streams) {
        Runtime.getRuntime().addShutdownHook(new Thread(streams::close));
    }
}
