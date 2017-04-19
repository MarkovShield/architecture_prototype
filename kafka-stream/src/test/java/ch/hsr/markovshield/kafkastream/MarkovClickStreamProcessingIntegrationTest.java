package ch.hsr.markovshield.kafkastream;

import ch.hsr.markovshield.models.Click;
import ch.hsr.markovshield.models.ClickStream;
import ch.hsr.markovshield.models.Session;
import ch.hsr.markovshield.models.UrlRating;
import ch.hsr.markovshield.models.UserModel;
import ch.hsr.markovshield.utils.JsonPOJOSerde;
import io.confluent.examples.streams.IntegrationTestUtils;
import io.confluent.examples.streams.kafka.EmbeddedSingleNodeKafkaCluster;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.LongDeserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.test.TestUtils;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import java.sql.Date;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;

public class MarkovClickStreamProcessingIntegrationTest {

    @ClassRule
    public static final EmbeddedSingleNodeKafkaCluster CLUSTER = new EmbeddedSingleNodeKafkaCluster();

    private static final String clickTopic = MarkovClickStreamProcessing.MARKOV_CLICK_TOPIC;
    private static final String loginTopic = MarkovClickStreamProcessing.MARKOV_LOGIN_TOPIC;
    private static final String modelTopic = MarkovClickStreamProcessing.MARKOV_USER_MODEL_TOPIC;
    private static final String clickStreamTopic = MarkovClickStreamProcessing.MARKOV_CLICK_STREAM_TOPIC;
    private static final String analysisTopic = MarkovClickStreamProcessing.MARKOV_CLICK_STREAM_ANALYSIS_TOPIC;
    private static final Serde stringSerde = MarkovClickStreamProcessing.stringSerde;
    private static final JsonPOJOSerde<Click> clickSerde = MarkovClickStreamProcessing.clickSerde;
    private static final JsonPOJOSerde<Session> sessionSerde = MarkovClickStreamProcessing.sessionSerde;
    private static final JsonPOJOSerde<UserModel> userModelSerde = MarkovClickStreamProcessing.userModelSerde;
    private static final JsonPOJOSerde<ClickStream> clickStreamSerde = MarkovClickStreamProcessing.clickStreamSerde;
    private Properties streamsConfiguration;

    @BeforeClass
    public static void startKafkaCluster() throws Exception {
        CLUSTER.createTopic(clickTopic);
        CLUSTER.createTopic(loginTopic);
        CLUSTER.createTopic(modelTopic);
        CLUSTER.createTopic(analysisTopic);
        CLUSTER.createTopic(clickStreamTopic);
    }

    @Before
    public void setUp() throws Exception {
        streamsConfiguration = new Properties();
        streamsConfiguration.put(StreamsConfig.APPLICATION_ID_CONFIG, "wordcount-lambda-integration-test");
        streamsConfiguration.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, CLUSTER.bootstrapServers());
        streamsConfiguration.put(StreamsConfig.KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        streamsConfiguration.put(StreamsConfig.VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        // The commit interval for flushing records to state stores and downstream must be lower than
        // this integration test's timeout (30 secs) to ensure we observe the expected processing results.
        streamsConfiguration.put(StreamsConfig.COMMIT_INTERVAL_MS_CONFIG, 10 * 1000);
        streamsConfiguration.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        // Use a temporary directory for storing state, which will be automatically removed after the test.
        streamsConfiguration.put(StreamsConfig.STATE_DIR_CONFIG, TestUtils.tempDirectory().getAbsolutePath());
    }

    @Test
    public void shouldAggregateClicksToClickStreamTopic() throws Exception {
        MarkovClickStreamProcessing clickStreamProcessing = new MarkovClickStreamProcessing();
        final KafkaStreams streams = new KafkaStreams(clickStreamProcessing.getStreamBuilder(), streamsConfiguration);
        streams.cleanUp();
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
        final List<Click> clicks = new LinkedList<>();
        String session1 = "1";
        String session2 = "2";
        clicks.add(new Click(session1,
            String.valueOf(1),
            "start.html",
            UrlRating.RISK_LEVEL_LOW,
            Date.from(
                Instant.now())));
        clicks.add(new Click(session1,
            String.valueOf(2),
            "login.html",
            UrlRating.RISK_LEVEL_MEDIUM,
            Date.from(
                Instant.now())));
        clicks.add(new Click(session2,
            String.valueOf(3),
            "start.html",
            UrlRating.RISK_LEVEL_LOW,
            Date.from(
                Instant.now())));
        clicks.add(new Click(session2,
            String.valueOf(4),
            "start.html",
            UrlRating.RISK_LEVEL_LOW,
            Date.from(
                Instant.now())));
        clicks.add(new Click(session1,
            String.valueOf(5),
            "xxx.html",
            UrlRating.RISK_LEVEL_HIGH,
            Date.from(
                Instant.now())));
        List collect = clicks.stream().map(click -> new KeyValue(click.getSessionUUID(), click)).collect(
            Collectors.toList());
        IntegrationTestUtils.produceKeyValuesSynchronously(clickTopic,
            collect,
            producerConfig,
            stringSerde.serializer(),
            clickSerde.serializer());

        //
        // Step 3: Verify the application's output data.
        //
        Properties consumerConfig = new Properties();
        consumerConfig.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, CLUSTER.bootstrapServers());
        consumerConfig.put(ConsumerConfig.GROUP_ID_CONFIG,
            "markov-chlick-stream-processing-integration-test-standard-consumer");
        consumerConfig.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        consumerConfig.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        consumerConfig.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, LongDeserializer.class);
        ArrayList<KeyValue<String, ClickStream>> expectedClickStreams = new ArrayList<>();
        expectedClickStreams.add(new KeyValue<>(session1,
            new ClickStream("--------------------NOT FOUND---------------------------",
                session1,
                clicks.stream().filter(click -> click.getSessionUUID() == session1).collect(
                    Collectors.toList()))));
        expectedClickStreams.add(new KeyValue<>(session2,
            new ClickStream("--------------------NOT FOUND---------------------------",
                session2,
                clicks.stream().filter(click -> click.getSessionUUID() == session2).collect(
                    Collectors.toList()))));
        List<KeyValue<String, ClickStream>> actualClickStreams = IntegrationTestUtils.waitUntilMinKeyValueRecordsReceived(
            consumerConfig,
            clickStreamTopic,
            2,
            30 * 1000L,

            new StringDeserializer(),
            clickStreamSerde.deserializer()
        );
        streams.close();
        assertThat(actualClickStreams, containsInAnyOrder(expectedClickStreams.toArray()));
    }
}