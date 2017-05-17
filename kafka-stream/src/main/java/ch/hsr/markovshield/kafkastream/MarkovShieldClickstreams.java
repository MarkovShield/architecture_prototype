package ch.hsr.markovshield.kafkastream;

import ch.hsr.markovshield.constants.MarkovTopics;
import io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.KStreamBuilder;
import org.apache.kafka.streams.processor.WallclockTimestampExtractor;
import org.apache.kafka.streams.state.HostInfo;
import java.util.Map;
import java.util.Properties;

public class MarkovShieldClickstreams {

    public static final String KAFKA_JOB_NAME = "MarkovShieldClickstreams";
    public static final String DEFAULT_BOOTSTRAP_SERVERS = "localhost:9092";
    public static final String DEFAULT_SCHEMA_REGISTRY_URL = "http://localhost:8081";
    private static final String ZOOKEEPER = "zookeeper:2181";
    private static final String DEFAULT_REST_ENDPOINT_HOSTNAME = "localhost";
    private static final int DEFAULT_REST_ENDPOINT_PORT = 7777;

    public static void main(final String[] args) throws Exception {
        if (args.length == 0 || args.length > 4) {
            throw new IllegalArgumentException("usage: ... <portForRestEndpoint> " +
                "[<bootstrap.servers> (optional, default: " + DEFAULT_BOOTSTRAP_SERVERS + ")] " +
                "[<schema.registry.url> (optional, default: " + DEFAULT_SCHEMA_REGISTRY_URL + ")] " +
                "[<hostnameForRestEndPoint> (optional, default: " + DEFAULT_REST_ENDPOINT_HOSTNAME + ")]");
        }
        final int restEndpointPort = args.length > 0 ? Integer.valueOf(args[0]) : DEFAULT_REST_ENDPOINT_PORT;
        final String bootstrapServers = args.length > 1 ? args[1] : "localhost:9092";
        final String schemaRegistryUrl = args.length > 2 ? args[2] : "http://localhost:8081";
        final String restEndpointHostname = args.length > 3 ? args[3] : DEFAULT_REST_ENDPOINT_HOSTNAME;
        final HostInfo restEndpoint = new HostInfo(restEndpointHostname, restEndpointPort);

        final Properties streamsConfiguration = getStreamConfiguration(restEndpoint);
        setUpKafka(streamsConfiguration);
        StreamProcessing streamProcessing = new MarkovClickStreamProcessing();
        KStreamBuilder streamBuilder = streamProcessing.getStreamBuilder();
        StreamingApplication streamingApplication = new StreamingApplication(streamsConfiguration, streamBuilder);
        streamingApplication.startStreamingApp(restEndpoint);
    }

    private static void setUpKafka(Properties streamsConfiguration) {
        Properties topicCreatorProperties = (Properties) streamsConfiguration.clone();
        topicCreatorProperties.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        topicCreatorProperties.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        KafkaConsumer consumer = new KafkaConsumer(topicCreatorProperties);
        Map map = consumer.listTopics();
        KafkaTopicCreator kafkaTopicCreator = new KafkaTopicCreator(ZOOKEEPER);
        createTopicIfNotPresent(map, kafkaTopicCreator, MarkovTopics.MARKOV_CLICK_STREAM_ANALYSIS_TOPIC);
        createTopicIfNotPresent(map, kafkaTopicCreator, MarkovTopics.MARKOV_CLICK_STREAM_TOPIC);
        createTopicIfNotPresent(map, kafkaTopicCreator, MarkovTopics.MARKOV_LOGIN_TOPIC);
        createTopicIfNotPresent(map, kafkaTopicCreator, MarkovTopics.MARKOV_USER_MODEL_TOPIC);
        createTopicIfNotPresent(map, kafkaTopicCreator, MarkovTopics.MARKOV_CLICK_TOPIC);
        kafkaTopicCreator.closeConnection();
    }

    private static void createTopicIfNotPresent(Map map, KafkaTopicCreator kafkaTopicCreator, String topic) {
        if (!map.containsKey(topic)) {
            kafkaTopicCreator.createTopic(topic);
        }
    }

    private static Properties getStreamConfiguration(HostInfo restEndpoint) {
        final Serde<String> stringSerde = Serdes.String();
        final Properties streamsConfiguration = new Properties();
        streamsConfiguration.put(StreamsConfig.APPLICATION_ID_CONFIG, KAFKA_JOB_NAME);
        streamsConfiguration.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, DEFAULT_BOOTSTRAP_SERVERS);
        streamsConfiguration.put(AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, DEFAULT_SCHEMA_REGISTRY_URL);
        streamsConfiguration.put(StreamsConfig.KEY_SERDE_CLASS_CONFIG, stringSerde.getClass().getName());
        streamsConfiguration.put(StreamsConfig.VALUE_SERDE_CLASS_CONFIG, stringSerde.getClass().getName());
        streamsConfiguration.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        streamsConfiguration.put(StreamsConfig.COMMIT_INTERVAL_MS_CONFIG, 1000);
        streamsConfiguration.put(StreamsConfig.APPLICATION_SERVER_CONFIG,
            restEndpoint.host() + ":" + restEndpoint.port());
        streamsConfiguration.put(StreamsConfig.STATE_DIR_CONFIG, "kafka-store");
        streamsConfiguration.put(StreamsConfig.TIMESTAMP_EXTRACTOR_CLASS_CONFIG, WallclockTimestampExtractor.class);
        return streamsConfiguration;
    }
}


