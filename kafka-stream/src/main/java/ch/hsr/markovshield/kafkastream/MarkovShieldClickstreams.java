package ch.hsr.markovshield.kafkastream;

import ch.hsr.markovshield.models.Click;
import ch.hsr.markovshield.models.ClickStream;
import ch.hsr.markovshield.models.Session;
import ch.hsr.markovshield.models.UrlRating;
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
    public static final String MARKOV_CLICK_STREAM_ANALYSIS_TOPIC = "MarkovClickStreamAnalysis";

    public static void main(final String[] args) throws Exception {
        final Properties streamsConfiguration = getStreamConfiguration();
        final KStreamBuilder builder = new KStreamBuilder();
        final KTable<String, Session> sessions = getSessionTable(builder);
        final KTable<String, UserModel> userModels = getUserModelTable(builder);
        final KStream<String, Click> views = getClickStream(builder);

        KTable<String, ClickStream> clickstreams = aggregateClicks(sessions, views);

        KStream<String, ClickStream> stringValidationClickStreamKStream = filterByThreshold(clickstreams);

        KStream<String, ValidationClickStream> clickStreamsWithModel = addModelToClickStreams(userModels,
            stringValidationClickStreamKStream);

        outputClickStreams(clickstreams);

        outputClickstreamsForAnalysis(clickStreamsWithModel);

        stringValidationClickStreamKStream.print();
        userModels.foreach((key, value) -> System.out.println("UserModel: " + key + " " + value.toString()));
        sessions.foreach((key, value) -> System.out.println("Session: " + key + " " + value.toString()));
        views.foreach((key, value) -> System.out.println("Click: " + key + " " + value.toString()));
        clickStreamsWithModel.print();

        startStreamingApp(streamsConfiguration, builder);
    }

    private static void startStreamingApp(Properties streamsConfiguration, KStreamBuilder builder) {
        final KafkaStreams streams = new KafkaStreams(builder, streamsConfiguration);
        streams.cleanUp();
        streams.start();
        // Add shutdown hook to respond to SIGTERM and gracefully close Kafka Streams
        addShutdown(streams);
    }

    private static void addShutdown(KafkaStreams streams) {
        Runtime.getRuntime().addShutdownHook(new Thread(streams::close));
    }

    private static void outputClickstreamsForAnalysis(KStream<String, ValidationClickStream> clickStreamsWithModel) {
        clickStreamsWithModel.to(Serdes.String(),
            new JsonPOJOSerde<>(ValidationClickStream.class),
            MARKOV_CLICK_STREAM_ANALYSIS_TOPIC);
    }

    private static void outputClickStreams(KTable<String, ClickStream> clickstreams) {
        clickstreams.to(Serdes.String(), new JsonPOJOSerde<>(ClickStream.class), MARKOV_CLICK_STREAM_TOPIC);
    }

    private static KStream<String, ValidationClickStream> addModelToClickStreams(KTable<String, UserModel> userModels, KStream<String, ClickStream> stringValidationClickStreamKStream) {
        return stringValidationClickStreamKStream.mapValues(
            ValidationClickStream::fromClickStream).leftJoin(
            userModels,
            (ValidationClickStream clickStream, UserModel userModel) -> {
                clickStream.setUserModel(userModel);
                return clickStream;
            },
            Serdes.String(),
            new JsonPOJOSerde<>(ValidationClickStream.class));
    }

    private static KStream<String, ClickStream> filterByThreshold(KTable<String, ClickStream> clickstreams) {
        return clickstreams
            .toStream((s, clickStream) -> clickStream.getUserName()).filter((s, validationClickStream) -> {
                Optional<Boolean> overThreshold = validationClickStream.lastClick()
                    .map(MarkovShieldClickstreams::isUrlOverThreshold);
                return overThreshold.orElse(false);
            });
    }

    private static KTable<String, ClickStream> aggregateClicks(KTable<String, Session> sessions, KStream<String, Click> views) {
        return views.leftJoin(sessions,
            (view, session) -> {
                String newUserName;
                if (session != null) {
                    newUserName = session.getUserName();
                } else {
                    newUserName = USER_NOT_FOUND;
                }
                return new ClickStream(newUserName, view.getSessionUUID(), Collections.singletonList(view));
            }, Serdes.String(), new JsonPOJOSerde<>(Click.class)
        ).groupByKey(Serdes.String(), new JsonPOJOSerde<>(ClickStream.class)).reduce(
            (clickStream, v1) -> {
                String userName = clickStream.getUserName();
                if (clickStream.getUserName().equals(USER_NOT_FOUND) && !(clickStream.getUserName()
                    .equals(v1.getUserName()))) {
                    userName = v1.getUserName();
                }
                return new ClickStream(userName,
                    clickStream.getSessionUUID(),
                    Lists.newLinkedList(concat(clickStream.getClicks(), v1.getClicks())));
            }, "MarkovClickStreamAggregation"
        );
    }

    private static KStream<String, Click> getClickStream(KStreamBuilder builder) {
        return builder.stream(Serdes.String(),
            new JsonPOJOSerde<>(Click.class),
            MARKOV_CLICK_TOPIC);
    }

    private static Properties getStreamConfiguration() {
        final Properties streamsConfiguration = new Properties();

        streamsConfiguration.put(StreamsConfig.APPLICATION_ID_CONFIG, KAFKA_JOB_NAME);
        streamsConfiguration.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, BROKER);
        streamsConfiguration.put(AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, SCHEMA_REGISTRY);
        streamsConfiguration.put(StreamsConfig.KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        streamsConfiguration.put(StreamsConfig.VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        streamsConfiguration.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        streamsConfiguration.put(StreamsConfig.COMMIT_INTERVAL_MS_CONFIG, 1);
        return streamsConfiguration;
    }

    private static KTable<String, UserModel> getUserModelTable(KStreamBuilder builder) {
        return builder.table(Serdes.String(),
            new JsonPOJOSerde<>(UserModel.class),
            MARKOV_USER_MODEL_TOPIC,
            "MarkovUserModelStore");
    }

    private static KTable<String, Session> getSessionTable(KStreamBuilder builder) {
        return builder.table(Serdes.String(),
            new JsonPOJOSerde<>(Session.class),
            MARKOV_LOGIN_TOPIC,
            "MarkovLoginStore");
    }

    private static Boolean isUrlOverThreshold(Click click) {
        return click.getUrlRiskLevel() == UrlRating.RISK_LEVEL_MEDIUM || click.getUrlRiskLevel() == UrlRating.RISK_LEVEL_HIGH;
    }

}


