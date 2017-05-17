package ch.hsr.markovshield.kafkastream;

import io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.KStreamBuilder;
import org.apache.kafka.streams.processor.WallclockTimestampExtractor;
import java.util.Map;
import java.util.Properties;

public class MarkovShieldClickstreams {

    public static final String KAFKA_JOB_NAME = "MarkovShieldClickstreams";
    public static final String BROKER = "localhost:9092";
    public static final String SCHEMA_REGISTRY = "http://localhost:8081";
    private static final String ZOOKEEPER = "zookeeper:2181";

    public static void main(final String[] args) throws Exception {
        final Properties streamsConfiguration = getStreamConfiguration();
        setUpKafka(streamsConfiguration);
        StreamProcessing streamProcessing = new MarkovClickStreamProcessing();
        KStreamBuilder streamBuilder = streamProcessing.getStreamBuilder();
        StreamingApplication streamingApplication = new StreamingApplication(streamsConfiguration, streamBuilder);
        streamingApplication.startStreamingApp();
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

    private static Properties getStreamConfiguration() {
        final Serde<String> stringSerde = Serdes.String();
        final Properties streamsConfiguration = new Properties();
        streamsConfiguration.put(StreamsConfig.APPLICATION_ID_CONFIG, KAFKA_JOB_NAME);
        streamsConfiguration.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, BROKER);
        streamsConfiguration.put(AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, SCHEMA_REGISTRY);
        streamsConfiguration.put(StreamsConfig.KEY_SERDE_CLASS_CONFIG, stringSerde.getClass().getName());
        streamsConfiguration.put(StreamsConfig.VALUE_SERDE_CLASS_CONFIG, stringSerde.getClass().getName());
        streamsConfiguration.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        streamsConfiguration.put(StreamsConfig.COMMIT_INTERVAL_MS_CONFIG, 1000);
        streamsConfiguration.put(StreamsConfig.APPLICATION_SERVER_CONFIG, "localhost:" + 7777);
        streamsConfiguration.put(StreamsConfig.STATE_DIR_CONFIG, "kafka-store");
        streamsConfiguration.put(StreamsConfig.TIMESTAMP_EXTRACTOR_CLASS_CONFIG, WallclockTimestampExtractor.class);
        return streamsConfiguration;
    }
}


