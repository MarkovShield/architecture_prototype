package ch.hsr.markovshield.kafkastream;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.LongDeserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.common.utils.Time;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.integration.utils.IntegrationTestUtils;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KStreamBuilder;
import org.junit.BeforeClass;
import org.junit.ClassRule;

import io.confluent.examples.streams.kafka.EmbeddedSingleNodeKafkaCluster;
import org.junit.Test;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.regex.Pattern;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;


public class MarkovShieldClickstreamsTest {
    @ClassRule
    public static final EmbeddedSingleNodeKafkaCluster CLUSTER = new EmbeddedSingleNodeKafkaCluster();
    private static final String inputTopic = "inputTopic";
    private static final String rekeyedIntermediateTopic = "rekeyedIntermediateTopic";
    private static final String outputTopic = "outputTopic";

    @BeforeClass
    public static void startKafkaCluster() throws Exception {
        CLUSTER.createTopic(inputTopic, 2, 1);
        CLUSTER.createTopic(rekeyedIntermediateTopic);
        CLUSTER.createTopic(outputTopic);
    }

    @Test
    public void shouldCountWords() throws Exception {
        List<String> inputValues = Arrays.asList(
            "Hello Kafka Streams",
            "All streams lead to Kafka",
            "Join Kafka Summit",
            "И теперь пошли русские слова"
        );
        List<KeyValue<String, Long>> expectedWordCounts = Arrays.asList(
            new KeyValue<>("hello", 1L),
            new KeyValue<>("kafka", 1L),
            new KeyValue<>("streams", 1L),
            new KeyValue<>("all", 1L),
            new KeyValue<>("streams", 2L),
            new KeyValue<>("lead", 1L),
            new KeyValue<>("to", 1L),
            new KeyValue<>("kafka", 2L),
            new KeyValue<>("join", 1L),
            new KeyValue<>("kafka", 3L),
            new KeyValue<>("summit", 1L),
            new KeyValue<>("и", 1L),
            new KeyValue<>("теперь", 1L),
            new KeyValue<>("пошли", 1L),
            new KeyValue<>("русские", 1L),
            new KeyValue<>("слова", 1L)
        );

        //
        // Step 1: Configure and start the processor topology.
        //
        // This code is identical to what's shown in WordCountLambdaExample, with the exception of
        // the call to `purgeLocalStreamsState()`, which we need only for cleaning up previous runs of
        // this integration test.
        //
        final Serde<String> stringSerde = Serdes.String();
        final Serde<Long> longSerde = Serdes.Long();

        Properties streamsConfiguration = new Properties();
        streamsConfiguration.put(StreamsConfig.APPLICATION_ID_CONFIG, "wordcount-lambda-integration-test");
        streamsConfiguration.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, CLUSTER.bootstrapServers());
        streamsConfiguration.put(StreamsConfig.ZOOKEEPER_CONNECT_CONFIG, CLUSTER.zookeeperConnect());
        streamsConfiguration.put(StreamsConfig.KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        streamsConfiguration.put(StreamsConfig.VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        streamsConfiguration.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        // Explicitly place the state directory under /tmp so that we can remove it via
        // `purgeLocalStreamsState` below.  Once Streams is updated to expose the effective
        // StreamsConfig configuration (so we can retrieve whatever state directory Streams came up
        // with automatically) we don't need to set this anymore and can update `purgeLocalStreamsState`
        // accordingly.
        streamsConfiguration.put(StreamsConfig.STATE_DIR_CONFIG, "/tmp/kafka-streams");

        // Remove any state from previous test runs
        IntegrationTestUtils.purgeLocalStreamsState(streamsConfiguration);

        KStreamBuilder builder = new KStreamBuilder();

        KStream<String, String> textLines = builder.stream(inputTopic);
        Pattern pattern = Pattern.compile("\\W+", Pattern.UNICODE_CHARACTER_CLASS);
        KStream<String, Long> wordCounts = textLines
            .flatMapValues(value -> Arrays.asList(pattern.split(value.toLowerCase())))
            .map((key, word) -> new KeyValue<>(word, word))
            // Required only in 0.10.0 to re-partition the data because we re-keyed the stream in the
            // `map` step.  Upcoming Kafka 0.10.1 does this automatically (no need for `through`).
            .through(rekeyedIntermediateTopic).groupByKey().count("Counts")
            .toStream();

        wordCounts.to(stringSerde, longSerde, outputTopic);

        KafkaStreams streams = new KafkaStreams(builder, streamsConfiguration);
        streams.start();

        //
        // Step 2: Produce some input data to the input topic.
        //
        Properties producerConfig = new Properties();
        producerConfig.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, CLUSTER.bootstrapServers());
        producerConfig.put(ProducerConfig.ACKS_CONFIG, "all");
        producerConfig.put(ProducerConfig.RETRIES_CONFIG, 0);
        producerConfig.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        producerConfig.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        IntegrationTestUtils.produceValuesSynchronously(inputTopic, inputValues, producerConfig, Time.SYSTEM);

        //
        // Step 3: Verify the application's output data.
        //
        Properties consumerConfig = new Properties();
        consumerConfig.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, CLUSTER.bootstrapServers());
        consumerConfig.put(ConsumerConfig.GROUP_ID_CONFIG, "wordcount-lambda-integration-test-standard-consumer");
        consumerConfig.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        consumerConfig.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        consumerConfig.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, LongDeserializer.class);
        List<KeyValue<String, Long>> actualWordCounts = IntegrationTestUtils.waitUntilMinKeyValueRecordsReceived(consumerConfig,
            outputTopic, expectedWordCounts.size());
        streams.close();
        assertThat(actualWordCounts, equalTo(expectedWordCounts));
    }

}
