package ch.hsr.markovshield.kafkastream.application;

import ch.hsr.markovshield.kafkastream.streaming.MarkovClickStreamProcessing;
import ch.hsr.markovshield.models.Click;
import ch.hsr.markovshield.models.ClickStream;
import ch.hsr.markovshield.models.Session;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.GlobalKTable;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KStreamBuilder;
import org.apache.kafka.streams.kstream.KTable;
import java.io.IOException;
import java.time.Instant;
import java.util.Properties;

import static ch.hsr.markovshield.constants.MarkovTopics.MARKOV_LOGIN_TOPIC;

public class ExampleStreamsApp {

    public static final Serde<String> stringSerde = Serdes.String();

    public static void main(final String[] args) throws IOException, InterruptedException {
        final Properties properties = new Properties();
        properties.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        properties.put(StreamsConfig.ZOOKEEPER_CONNECT_CONFIG, "zookeeper:2181");

        properties.put(StreamsConfig.KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        properties.put(StreamsConfig.VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        properties.put(StreamsConfig.APPLICATION_ID_CONFIG, "ExampleStreams");

        KStreamBuilder builder = new KStreamBuilder();
        KStream<String, String> example_topic = builder
            .stream(stringSerde, stringSerde,
                "EXAMPLE_TOPIC");

        example_topic.mapValues(click -> {
            long l = Instant.now().toEpochMilli();
            long l1 = l - Long.valueOf(click);
            return click + " " + l + " " + l1;
        }).print();
        example_topic.to("EXAMPLE_TOPIC2");



        KafkaStreams streams = new KafkaStreams(builder, properties);
        streams.start();
        Runtime.getRuntime().addShutdownHook(new Thread(streams::close));

    }


}
