package ch.hsr.markovshield.kafkastream;

import ch.hsr.markovshield.ml.FrequencyMatrix;
import ch.hsr.markovshield.ml.MarkovChainWithMatrix;
import ch.hsr.markovshield.models.Click;
import ch.hsr.markovshield.models.ClickStream;
import ch.hsr.markovshield.models.MatrixFrequencyModel;
import ch.hsr.markovshield.models.Session;
import ch.hsr.markovshield.models.UrlRating;
import ch.hsr.markovshield.models.UrlStore;
import ch.hsr.markovshield.models.UserModel;
import ch.hsr.markovshield.models.ValidationClickStream;
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
import org.junit.Test;
import java.sql.Date;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;

public class MarkovClickStreamProcessingIntegrationTest {

    private static final String clickTopic = MarkovTopics.MARKOV_CLICK_TOPIC;
    private static final String loginTopic = MarkovTopics.MARKOV_LOGIN_TOPIC;
    private static final String modelTopic = MarkovTopics.MARKOV_USER_MODEL_TOPIC;
    private static final String clickStreamTopic = MarkovTopics.MARKOV_CLICK_STREAM_TOPIC;
    private static final String analysisTopic = MarkovTopics.MARKOV_CLICK_STREAM_ANALYSIS_TOPIC;
    private static final Serde stringSerde = MarkovClickStreamProcessing.stringSerde;
    private static final JsonPOJOSerde<Click> clickSerde = MarkovClickStreamProcessing.clickSerde;
    private static final JsonPOJOSerde<Session> sessionSerde = MarkovClickStreamProcessing.sessionSerde;
    private static final JsonPOJOSerde<UserModel> userModelSerde = MarkovClickStreamProcessing.userModelSerde;
    private static final JsonPOJOSerde<ClickStream> clickStreamSerde = MarkovClickStreamProcessing.clickStreamSerde;
    private static final JsonPOJOSerde<ValidationClickStream> clickStreamValidationSerde = MarkovClickStreamProcessing.validationClickStreamSerde;
    private EmbeddedSingleNodeKafkaCluster cluster;
    private Properties streamsConfiguration;


    @Before
    public void setUp() throws Exception {
        cluster = new EmbeddedSingleNodeKafkaCluster();
        cluster.start();
        cluster.createTopic(clickTopic);
        cluster.createTopic(loginTopic);
        cluster.createTopic(modelTopic);
        cluster.createTopic(analysisTopic);
        cluster.createTopic(clickStreamTopic);
        streamsConfiguration = new Properties();
        streamsConfiguration.put(StreamsConfig.APPLICATION_ID_CONFIG, "wordcount-lambda-integration-test");
        streamsConfiguration.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, cluster
            .bootstrapServers());
        streamsConfiguration.put(StreamsConfig.KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        streamsConfiguration.put(StreamsConfig.VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        // The commit interval for flushing records to state stores and downstream must be lower than
        // this integration test's timeout (30 secs) to ensure we observe the expected processing results.
        streamsConfiguration.put(StreamsConfig.COMMIT_INTERVAL_MS_CONFIG, 10 * 1000);
        streamsConfiguration.put(StreamsConfig.CACHE_MAX_BYTES_BUFFERING_CONFIG, 1);

        streamsConfiguration.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        // Use a temporary directory for storing state, which will be automatically removed after the test.
        streamsConfiguration.put(StreamsConfig.STATE_DIR_CONFIG, TestUtils.tempDirectory().getAbsolutePath());
    }

    @Test
    public void shouldAggregateClicksToValidationClickStreamTopic() throws Exception {
        MarkovClickStreamProcessing clickStreamProcessing = new MarkovClickStreamProcessing();
        final KafkaStreams streams = new KafkaStreams(clickStreamProcessing.getStreamBuilder(), streamsConfiguration);
        streams.cleanUp();
        streams.start();


        //
        // Step 2: Produce some input data to the input topic.
        //
        Properties producerConfig = getProducerProperties();

        String session1 = "1";
        String session2 = "2";
        String user1 = "user1";
        String user2 = "user2";
        List<Session> logins = new ArrayList<>();
        Session login1 = new Session(session1, user1);
        logins.add(login1);
        Session login2 = new Session(session2, user2);
        logins.add(login2);
        List sessionKeyValues = logins.stream().map(session -> new KeyValue(session.getSessionUUID(), session)).collect(
            Collectors.toList());
        IntegrationTestUtils.produceKeyValuesSynchronously(loginTopic,
            sessionKeyValues,
            producerConfig,
            stringSerde.serializer(),
            sessionSerde.serializer());
        List<UserModel> userModels = new ArrayList<>();
        FrequencyMatrix frequencyMatrix = null;
        UrlStore urlStore = null;
        UserModel user1Model = new UserModel(user1,
            MarkovChainWithMatrix.train(Collections.emptyList()),
            new MatrixFrequencyModel(frequencyMatrix, urlStore));
        userModels.add(user1Model);
        UserModel user2Model = new UserModel(user2,
            MarkovChainWithMatrix.train(Collections.emptyList()),
            new MatrixFrequencyModel(frequencyMatrix, urlStore));
        userModels.add(user2Model);
        List userModelsKeyValue = userModels.stream()
            .map(userModel -> new KeyValue(userModel.getUserId(), userModel))
            .collect(
                Collectors.toList());
        IntegrationTestUtils.produceKeyValuesSynchronously(modelTopic,
            userModelsKeyValue,
            producerConfig,
            stringSerde.serializer(),
            userModelSerde.serializer());

        final List<Click> clicks = new LinkedList<>();

        clicks.add(new Click(session1,
            String.valueOf(1),
            "start.html",
            UrlRating.RISK_LEVEL_LOW,
            Date.from(
                Instant.now()), false));
        clicks.add(new Click(session1,
            String.valueOf(2),
            "login.html",
            UrlRating.RISK_LEVEL_MEDIUM,
            Date.from(
                Instant.now()), false));
        clicks.add(new Click(session2,
            String.valueOf(3),
            "start.html",
            UrlRating.RISK_LEVEL_LOW,
            Date.from(
                Instant.now()), false));
        clicks.add(new Click(session2,
            String.valueOf(4),
            "start.html",
            UrlRating.RISK_LEVEL_LOW,
            Date.from(
                Instant.now()), false));
        clicks.add(new Click(session1,
            String.valueOf(5),
            "xxx.html",
            UrlRating.RISK_LEVEL_HIGH,
            Date.from(
                Instant.now()), true));
        clicks.add(new Click(session1,
            String.valueOf(6),
            "xxx.html",
            UrlRating.RISK_LEVEL_LOW,
            Date.from(
                Instant.now()), false));
        List clickKeyValues = clicks.stream().map(click -> new KeyValue(click.getSessionUUID(), click)).collect(
            Collectors.toList());
        IntegrationTestUtils.produceKeyValuesSynchronously(clickTopic,
            clickKeyValues,
            producerConfig,
            stringSerde.serializer(),
            clickSerde.serializer());


        //
        // Step 3: Verify the application's output data.
        //
        Properties consumerConfig = getConsumerProperties();
        ArrayList<KeyValue<String, ValidationClickStream>> expectedClickStreams = new ArrayList<>();
        expectedClickStreams.add(new KeyValue<>(user1,
            new ValidationClickStream(user1,
                session1,
                clicks.stream().filter(click -> Objects.equals(click.getSessionUUID(), session1)).collect(
                    Collectors.toList()).subList(0, 1),
                user1Model)));
        expectedClickStreams.add(new KeyValue<>(user1,
            new ValidationClickStream(user1,
                session1,
                clicks.stream().filter(click -> Objects.equals(click.getSessionUUID(), session1)).collect(
                    Collectors.toList()).subList(0, 2),
                user1Model)));
        expectedClickStreams.add(new KeyValue<>(user2,
            new ValidationClickStream(user2,
                session2,
                clicks.stream().filter(click -> Objects.equals(click.getSessionUUID(), session2)).collect(
                    Collectors.toList()).subList(0, 1),
                user2Model)));
        expectedClickStreams.add(new KeyValue<>(user2,
            new ValidationClickStream(user2,
                session2,
                clicks.stream().filter(click -> Objects.equals(click.getSessionUUID(), session2)).collect(
                    Collectors.toList()).subList(0, 2),
                user2Model)));
        expectedClickStreams.add(new KeyValue<>(user1,
            new ValidationClickStream(user1,
                session1,
                clicks.stream().filter(click -> Objects.equals(click.getSessionUUID(), session1)).collect(
                    Collectors.toList()).subList(0, 3),
                user1Model)));
        expectedClickStreams.add(new KeyValue<>(user1,
            new ValidationClickStream(user1,
                session1,
                clicks.stream().filter(click -> Objects.equals(click.getSessionUUID(), session1)).collect(
                    Collectors.toList()).subList(0, 4),
                user1Model)));

        List<KeyValue<String, ValidationClickStream>> actualClickStreams = IntegrationTestUtils.waitUntilMinKeyValueRecordsReceived(
            consumerConfig,
            analysisTopic,
            6,
            30 * 1000L,

            new StringDeserializer(),
            clickStreamValidationSerde.deserializer()
        );
        streams.close();
        assertThat(actualClickStreams, hasSize(6));
        assertThat(actualClickStreams, containsInAnyOrder(expectedClickStreams.toArray()));
    }

    private Properties getConsumerProperties() {
        Properties consumerConfig = new Properties();
        consumerConfig.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, cluster.bootstrapServers());
        consumerConfig.put(ConsumerConfig.GROUP_ID_CONFIG,
            "markov-chlick-stream-processing-integration-test-standard-consumer");
        consumerConfig.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        consumerConfig.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        consumerConfig.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, LongDeserializer.class);
        return consumerConfig;
    }

    private Properties getProducerProperties() {
        Properties producerConfig = new Properties();
        producerConfig.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, cluster.bootstrapServers());
        producerConfig.put(ProducerConfig.ACKS_CONFIG, "all");
        producerConfig.put(ProducerConfig.RETRIES_CONFIG, 0);
        producerConfig.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        producerConfig.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        return producerConfig;
    }

    @Test
    public void shouldAggregateClicksToValidationClickStreamTopicWithLateLogin() throws Exception {


        MarkovClickStreamProcessing clickStreamProcessing = new MarkovClickStreamProcessing();
        final KafkaStreams streams = new KafkaStreams(clickStreamProcessing.getStreamBuilder(), streamsConfiguration);
        streams.cleanUp();
        streams.start();


        //
        // Step 2: Produce some input data to the input topic.
        //
        Properties producerConfig = getProducerProperties();


        Properties consumerConfig = getConsumerProperties();

        String session1 = "1";
        String session2 = "2";
        String user1 = "user1";
        String user2 = "user2";

        List<UserModel> userModels = new ArrayList<>();
        FrequencyMatrix frequencyMatrix = null;
        UrlStore urlStore = null;
        UserModel user1Model = new UserModel(user1,
            MarkovChainWithMatrix.train(Collections.emptyList()),
            new MatrixFrequencyModel(frequencyMatrix, urlStore));
        userModels.add(user1Model);
        UserModel user2Model = new UserModel(user2,
            MarkovChainWithMatrix.train(Collections.emptyList()),
            new MatrixFrequencyModel(frequencyMatrix, urlStore));
        userModels.add(user2Model);
        List userModelsKeyValue = userModels.stream()
            .map(userModel -> new KeyValue(userModel.getUserId(), userModel))
            .collect(
                Collectors.toList());
        IntegrationTestUtils.produceKeyValuesSynchronously(modelTopic,
            userModelsKeyValue,
            producerConfig,
            stringSerde.serializer(),
            userModelSerde.serializer());

        final List<Click> clicks = new LinkedList<>();

        clicks.add(new Click(session1,
            String.valueOf(1),
            "start.html",
            UrlRating.RISK_LEVEL_LOW,
            Date.from(
                Instant.now()), false));
        clicks.add(new Click(session2,
            String.valueOf(2),
            "start.html",
            UrlRating.RISK_LEVEL_LOW,
            Date.from(
                Instant.now()), false));
        List collect = clicks.stream().map(click -> new KeyValue(click.getSessionUUID(), click)).collect(
            Collectors.toList());
        IntegrationTestUtils.produceKeyValuesSynchronously(clickTopic,
            collect,
            producerConfig,
            stringSerde.serializer(),
            clickSerde.serializer());


        List<KeyValue<String, ValidationClickStream>> intermediateClickStreams = IntegrationTestUtils.waitUntilMinKeyValueRecordsReceived(
            consumerConfig,
            analysisTopic,
            2,
            30 * 1000L,

            new StringDeserializer(),
            clickStreamValidationSerde.deserializer()
        );
        assertThat(intermediateClickStreams, hasSize(2));


        List<Session> logins = new ArrayList<>();
        Session login1 = new Session(session1, user1);
        logins.add(login1);
        Session login2 = new Session(session2, user2);
        logins.add(login2);
        List sessionKeyValues = logins.stream().map(session -> new KeyValue(session.getSessionUUID(), session)).collect(
            Collectors.toList());
        IntegrationTestUtils.produceKeyValuesSynchronously(loginTopic,
            sessionKeyValues,
            producerConfig,
            stringSerde.serializer(),
            sessionSerde.serializer());

        final List<Click> clicksAfterLogins = new LinkedList<>();
        clicksAfterLogins.add(
            new Click(session1,
                String.valueOf(3),
                "xxx.html",
                UrlRating.RISK_LEVEL_HIGH,
                Date.from(
                    Instant.now()), true));
        clicksAfterLogins.add(
            new Click(session2,
                String.valueOf(4),
                "xxx.html",
                UrlRating.RISK_LEVEL_HIGH,
                Date.from(
                    Instant.now()), true));
        clicks.addAll(clicksAfterLogins);
        List collect1 = clicksAfterLogins.stream().map(click -> new KeyValue(click.getSessionUUID(), click)).collect(
            Collectors.toList());
        IntegrationTestUtils.produceKeyValuesSynchronously(clickTopic,
            collect1,
            producerConfig,
            stringSerde.serializer(),
            clickSerde.serializer());


        //
        // Step 3: Verify the application's output data.
        //

        ArrayList<KeyValue<String, ValidationClickStream>> expectedClickStreams = new ArrayList<>();
        String UNKOWN_USER = "--------------------NOT FOUND---------------------------";
        expectedClickStreams.add(new KeyValue<>(UNKOWN_USER,
            new ValidationClickStream(UNKOWN_USER,
                session1,
                clicks.stream().filter(click -> Objects.equals(click.getSessionUUID(), session1)).collect(
                    Collectors.toList()).subList(0, 1),
                null)));
        expectedClickStreams.add(new KeyValue<>(UNKOWN_USER,
            new ValidationClickStream(UNKOWN_USER,
                session2,
                clicks.stream().filter(click -> Objects.equals(click.getSessionUUID(), session2)).collect(
                    Collectors.toList()).subList(0, 1),
                null)));

        expectedClickStreams.add(new KeyValue<>(user1,
            new ValidationClickStream(user1,
                session1,
                clicks.stream().filter(click -> Objects.equals(click.getSessionUUID(), session1)).collect(
                    Collectors.toList()).subList(0, 2),
                user1Model)));
        expectedClickStreams.add(new KeyValue<>(user2,
            new ValidationClickStream(user2,
                session2,
                clicks.stream().filter(click -> Objects.equals(click.getSessionUUID(), session2)).collect(
                    Collectors.toList()).subList(0, 2),
                user2Model)));

        List<KeyValue<String, ValidationClickStream>> actualClickStreams = IntegrationTestUtils.waitUntilMinKeyValueRecordsReceived(
            consumerConfig,
            analysisTopic,
            2,
            30 * 1000L,

            new StringDeserializer(),
            clickStreamValidationSerde.deserializer()
        );
        actualClickStreams.addAll(intermediateClickStreams);
        streams.close();
        assertThat(actualClickStreams, hasSize(4));
        assertThat(actualClickStreams, containsInAnyOrder(expectedClickStreams.toArray()));

    }

}