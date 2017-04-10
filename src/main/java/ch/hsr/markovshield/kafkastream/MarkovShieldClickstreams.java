package ch.hsr.markovshield.kafkastream;

import ch.hsr.markovshield.models.Click;
import ch.hsr.markovshield.models.ClickStream;
import ch.hsr.markovshield.models.Session;
import ch.hsr.markovshield.models.UrlConfiguration;
import ch.hsr.markovshield.models.UserModel;
import ch.hsr.markovshield.models.ValidationClickStream;
import ch.hsr.markovshield.utils.JsonPOJOSerde;
import com.google.common.collect.Lists;
import io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KStreamBuilder;
import org.apache.kafka.streams.kstream.KTable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

import static com.google.common.collect.Iterables.concat;

public class MarkovShieldClickstreams {

    public static final String USER_NOT_FOUND = "--------------------NOT FOUND---------------------------";
    public static final String KAFKA_JOB_NAME = "MarkovShieldClickstreams";
    public static final String BROKER = "localhost:9092";
    public static final String SCHEMA_REGISTRY = "http://localhost:8081";
    public static final String MARKOV_LOGIN_TOPIC = "MarkovLogins";
    public static final String MARKOV_USER_MODEL_TOPIC = "MarkovUserModels";
    public static final String MARKOV_CLICK_TOPIC = "MarkovClicks";
    public static final String MARKOV_CLICK_STREAM_TOPIC = "MarkovClickStreams";
    private static final String MARKOV_CONFIG_TOPIC = "MarkovUrlConfig";
    private static final String MARKOV_CLICK_STREAM_ANALYSIS_TOPIC = "MarkovClickStreamAnalysis";

    public static void main(final String[] args) throws Exception {
        final Properties streamsConfiguration = new Properties();

        streamsConfiguration.put(StreamsConfig.APPLICATION_ID_CONFIG, KAFKA_JOB_NAME);
        streamsConfiguration.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, BROKER);
        streamsConfiguration.put(AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, SCHEMA_REGISTRY);
        streamsConfiguration.put(StreamsConfig.KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        streamsConfiguration.put(StreamsConfig.VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        streamsConfiguration.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        streamsConfiguration.put(StreamsConfig.COMMIT_INTERVAL_MS_CONFIG, 1);


        final Map<String, UrlConfiguration> configMap = new HashMap<>();
        final KStreamBuilder builder = new KStreamBuilder();
        final KTable<String, UrlConfiguration> config = builder.table(Serdes.String(),
            new JsonPOJOSerde<>(UrlConfiguration.class),
            MARKOV_CONFIG_TOPIC,
            "MarkovConfigStore");
        config.foreach((s, urlConfiguration) -> configMap.put(s, urlConfiguration));
        final KTable<String, Session> sessions = builder.table(Serdes.String(),
            new JsonPOJOSerde<>(Session.class),
            MARKOV_LOGIN_TOPIC,
            "MarkovLoginStore");
        final KTable<String, UserModel> userModels = builder.table(Serdes.String(),
            new JsonPOJOSerde<>(UserModel.class),
            MARKOV_USER_MODEL_TOPIC,
            "MarkovUserModelStore");

        userModels.foreach((key, value) -> System.out.println("UserModel: " + key + " " + value.toString()));

        sessions.foreach((key, value) -> System.out.println("Session: " + key + " " + value.toString()));

        final KStream<String, Click> views = builder.stream(Serdes.String(),
            new JsonPOJOSerde<>(Click.class),
            MARKOV_CLICK_TOPIC);

        views.foreach((key, value) -> System.out.println("Click: " + key + " " + value.toString()));


        KTable<String, ClickStream> clickstreams = views.leftJoin(sessions,
            (view, session) -> {
                String newUserName;
                if (session != null) {
                    newUserName = session.getUserName();
                } else {
                    newUserName = USER_NOT_FOUND;
                }
                return new ClickStream(newUserName, view.getSessionId(), Collections.singletonList(view));
            }, Serdes.String(), new JsonPOJOSerde<>(Click.class)
        ).groupByKey(Serdes.String(), new JsonPOJOSerde<>(ClickStream.class)).reduce(
            (clickStream, v1) -> {
                String userName = clickStream.getUserName();
                if (clickStream.getUserName().toString().equals(USER_NOT_FOUND) && !(clickStream.getUserName()
                    .equals(v1.getUserName()))) {
                    userName = v1.getUserName();
                }
                return new ClickStream(userName,
                    clickStream.getSessionId(),
                    Lists.newLinkedList(concat(clickStream.getClicks(), v1.getClicks())));
            }, "MarkovClickStreamAggregation"
        );

        KStream<String, ValidationClickStream> stringValidationClickStreamKStream = clickstreams
            .toStream((s, clickStream) -> clickStream.getUserName()).filter((s, validationClickStream) -> {
                Optional<Boolean> overThreshold = validationClickStream.lastClick()
                    .map(click -> isUrlOverThreshold(configMap, click));
                return overThreshold.orElse(false);
            }).mapValues(
                clickStream -> ValidationClickStream.fromClickstream(clickStream, configMap));

        stringValidationClickStreamKStream.print();

        KStream<String, ValidationClickStream> clickStreamsWithModel = stringValidationClickStreamKStream.leftJoin(
            userModels,
            (clickStream, userModel) -> new ValidationClickStream(clickStream.getUserName(),
                clickStream.getSessionId(),
                clickStream.getClicks(),
                userModel,
                configMap),
            Serdes.String(),
            new JsonPOJOSerde<>(ValidationClickStream.class));

        clickstreams.to(Serdes.String(), new JsonPOJOSerde<>(ClickStream.class), MARKOV_CLICK_STREAM_TOPIC);

        clickStreamsWithModel.print();
        clickStreamsWithModel.to(Serdes.String(),
            new JsonPOJOSerde<>(ValidationClickStream.class),
            MARKOV_CLICK_STREAM_ANALYSIS_TOPIC);


        final KafkaStreams streams = new KafkaStreams(builder, streamsConfiguration);

        streams.cleanUp();
        streams.start();

        // Add shutdown hook to respond to SIGTERM and gracefully close Kafka Streams
        Runtime.getRuntime().addShutdownHook(new Thread(streams::close));
    }

    private static Boolean isUrlOverThreshold(Map<String, UrlConfiguration> configMap, Click click) {
        if (configMap != null) {
            UrlConfiguration urlConfiguration = configMap.get(click.getUrl());
            if (urlConfiguration != null) {
                return urlConfiguration.getRating().getValue() > 2;
            }
        }
        return null;
    }

}


