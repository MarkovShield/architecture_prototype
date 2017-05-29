package ch.hsr.markovshield.kafkastream.application;

import ch.hsr.markovshield.avro.Click2;
import ch.hsr.markovshield.avro.Clickstream2;
import ch.hsr.markovshield.avro.Session2;
import ch.hsr.markovshield.avro.UserModel2;
import ch.hsr.markovshield.avro.ValidationClickStream2;
import ch.hsr.markovshield.constants.MarkovTopics;
import ch.hsr.markovshield.models.Click;
import ch.hsr.markovshield.models.ValidatedClickStream;
import ch.hsr.markovshield.utils.JsonPOJOSerde;
import com.google.common.collect.Lists;
import io.confluent.examples.streams.utils.SpecificAvroSerde;
import io.confluent.kafka.schemaregistry.client.CachedSchemaRegistryClient;
import io.confluent.kafka.schemaregistry.client.SchemaRegistryClient;
import io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static ch.hsr.markovshield.constants.MarkovTopics.MARKOV_CLICK_STREAM_ANALYSIS_TOPIC;
import static ch.hsr.markovshield.constants.MarkovTopics.MARKOV_LOGIN_TOPIC;
import static ch.hsr.markovshield.constants.MarkovTopics.MARKOV_USER_MODEL_TOPIC;
import static ch.hsr.markovshield.kafkastream.streaming.MarkovClickStreamProcessing.MARKOV_LOGIN_STORE;
import static ch.hsr.markovshield.kafkastream.streaming.MarkovClickStreamProcessing.MARKOV_USER_MODEL_STORE;
import static ch.hsr.markovshield.kafkastream.streaming.MarkovClickStreamProcessing.USER_NOT_FOUND;
import static ch.hsr.markovshield.kafkastream.streaming.MarkovClickStreamProcessing.validatedClickStreamSerde;
import static com.google.common.collect.Iterables.concat;

public class ExampleClickStreamsApp {

    public static final Serde<String> stringSerde = Serdes.String();
    public static final JsonPOJOSerde<Click> clickSerde = new JsonPOJOSerde<>(Click.class);

    public static void main(final String[] args) throws IOException, InterruptedException {
        final Properties properties = new Properties();

        String commitInterval;
        String threads;
        if (args.length > 0) {
            commitInterval = args[0];

        } else {
            commitInterval = "10";
        }
        if (args.length > 1) {
            threads = args[1];
        } else {
            threads = "1";
        }
        properties.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        properties.put(StreamsConfig.ZOOKEEPER_CONNECT_CONFIG, "zookeeper:2181");
        properties.put(AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG,
            "http://localhost:8081");
        properties.put(StreamsConfig.COMMIT_INTERVAL_MS_CONFIG, commitInterval);
        properties.put(StreamsConfig.NUM_STREAM_THREADS_CONFIG, threads);
        properties.put(StreamsConfig.VALUE_SERDE_CLASS_CONFIG, SpecificAvroSerde.class);
        Serde<String> stringSerde = Serdes.String();
        properties.put(StreamsConfig.KEY_SERDE_CLASS_CONFIG, stringSerde.getClass().getName());
        properties.put(StreamsConfig.APPLICATION_ID_CONFIG, "ExampleClickStreams");


        SchemaRegistryClient client = new CachedSchemaRegistryClient("http://localhost:8081", 1000);
        Map<String, String> schemaProps = new HashMap<>();
        schemaProps.put(AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, "http://localhost:8081");
        final Serde<Click2> click2Serde = new SpecificAvroSerde<>(client, schemaProps);
        final Serde<Session2> session2Serde = new SpecificAvroSerde<>(client, schemaProps);
        final Serde<UserModel2> userModel2Serde = new SpecificAvroSerde<>(client, schemaProps);
        final Serde<Clickstream2> clickStream2Serde = new SpecificAvroSerde<>(client, schemaProps);
        final Serde<ValidationClickStream2> validationClickStreamSerde = new SpecificAvroSerde<>(client, schemaProps);


        KStreamBuilder builder = new KStreamBuilder();
        KStream<String, Click2> markovClicks = builder
            .stream(stringSerde, click2Serde, "MarkovClicks2");

        final GlobalKTable<String, Session2> sessions = getSessionTable(builder, session2Serde);
        final GlobalKTable<String, UserModel2> userModels = getUserModelTable(builder, userModel2Serde);

        KStream<String, Clickstream2> markovClickStreamAggregation = markovClicks
            .groupByKey(stringSerde, click2Serde)
            .aggregate(() -> new Clickstream2(USER_NOT_FOUND, null, Collections.emptyList()),
                ExampleClickStreamsApp::aggregateClick,
                clickStream2Serde,
                "MarkovClickStreamAggregation2")
            .toStream();
        KStream<String, Clickstream2> clickStreamsWithSessions = addSessionToClickStream(sessions,
            markovClickStreamAggregation);
        clickStreamsWithSessions.mapValues(clickStream -> {
                System.out.println("reducedClickStreams: " + String.valueOf(Instant.now()
                    .toEpochMilli() - clickStream.getClicks()
                    .get(0)
                    .getKafkaFirstProcessedDate()
                ));
                return clickStream;
            }
        );
        KStream<String, ValidationClickStream2> clickStreamsWithModel = addModelToClickStreams(userModels,
            clickStreamsWithSessions);
        outputClickstreamsForAnalysis(clickStreamsWithModel, validationClickStreamSerde);
        getValidatedClickstreams(builder).mapValues(clickStream -> clickStream.timeStampOfLastClick()
            .toInstant()
            .toEpochMilli() - clickStream.getClickStreamValidation().getTimeCreated().toInstant().toEpochMilli());
        KafkaStreams streams = new KafkaStreams(builder, properties);
        streams.start();

        Runtime.getRuntime().addShutdownHook(new Thread(streams::close));

    }

    private static KStream<String, Clickstream2> addSessionToClickStream(GlobalKTable<String, Session2> sessions,
                                                                         KStream<String, Clickstream2> markovClickStreamAggregation) {
        KStream<String, Clickstream2> x = markovClickStreamAggregation.leftJoin(sessions,
            (s, clickStream) -> String.valueOf(clickStream.getSessionUUID()),
            (clickStream, session) -> {
                if (session != null) {
                    clickStream.setUserName(session.getUserName());
                }
                return clickStream;
            });
        return x;
    }

    private static KStream<String, ValidatedClickStream> getValidatedClickstreams(KStreamBuilder builder) {
        return builder.stream(stringSerde,
            validatedClickStreamSerde,
            MarkovTopics.MARKOV_VALIDATED_CLICK_STREAMS);
    }


    private static void outputClickstreamsForAnalysis(KStream<String, ValidationClickStream2> clickStreamsWithModel,
                                                      Serde<ValidationClickStream2> validationClickStreamSerde) {
        clickStreamsWithModel.mapValues(validationClickStream -> {
            System.out.println("outputToKafka: " + String.valueOf(Instant.now()
                .toEpochMilli() - timeStampOfLastClick(validationClickStream)));
            return validationClickStream;
        })
            .to(stringSerde, validationClickStreamSerde, MARKOV_CLICK_STREAM_ANALYSIS_TOPIC + "2");
    }

    private static long timeStampOfLastClick(ValidationClickStream2 clickstream2) {
        return clickstream2.getClicks()
            .get(clickstream2.getClicks().size() - 1)
            .getTimeStamp();
    }

    private static long timeStampOfLastClick(Clickstream2 clickstream2) {
        return clickstream2.getClicks()
            .get(clickstream2.getClicks().size() - 1)
            .getTimeStamp();
    }

    private static GlobalKTable<String, UserModel2> getUserModelTable(KStreamBuilder builder,
                                                                      Serde<UserModel2> userModel2Serde) {
        return builder
            .globalTable(stringSerde, userModel2Serde, MARKOV_USER_MODEL_TOPIC + "2",
                MARKOV_USER_MODEL_STORE + "2");
    }


    private static KTable<String, Clickstream2> aggregateClicks(GlobalKTable<String, Session2> sessions,
                                                                KStream<String, Click2> clicks) {
        KStream<String, Clickstream2> stringRVKStream = clicks.leftJoin(sessions,
            (s, click) -> String.valueOf(click.getSessionUUID()),
            ExampleClickStreamsApp::getInitialClickStream);

        return stringRVKStream
            .groupByKey()
            .reduce(ExampleClickStreamsApp::reduceClickStreams, "MarkovClickStreamAggregation2");
    }

    private static Clickstream2 aggregateClick(String sessionId, Click2 click, Clickstream2 clickStream) {
        if (clickStream.getSessionUUID() == null) {
            clickStream.setSessionUUID(click.getSessionUUID());
        }
        ArrayList<Click2> click2s = new ArrayList<>(clickStream.getClicks());
        click2s.add(click);
        clickStream.setClicks(click2s);
        System.out.println("reduceClickStreams: " + String.valueOf(Instant.now()
            .toEpochMilli() - timeStampOfLastClick(clickStream)));
        return clickStream;

    }

    private static GlobalKTable<String, Session2> getSessionTable(KStreamBuilder builder,
                                                                  Serde<Session2> session2Serde) {
        return builder.globalTable(stringSerde, session2Serde, MARKOV_LOGIN_TOPIC + "2",
            MARKOV_LOGIN_STORE + "2");
    }

    private static Clickstream2 getInitialClickStream(Click2 click, Session2 session) {
        String newUserName = session != null ? String.valueOf(session.getUserName()) : USER_NOT_FOUND;
        return new Clickstream2(newUserName, click.getSessionUUID(), Collections.singletonList(click));
    }

    private static Clickstream2 reduceClickStreams(Clickstream2 clickStream, Clickstream2 anotherClickStream) {
        String userName = String.valueOf(clickStream.getUserName());
        if (isUserNameIsNotSet(clickStream, anotherClickStream)) {
            userName = String.valueOf(anotherClickStream.getUserName());
        }
        System.out.println("reduceClickStreams: " + String.valueOf(Instant.now()
            .toEpochMilli() - timeStampOfLastClick(clickStream)));
        return new Clickstream2(userName,
            clickStream.getSessionUUID(),
            Lists.newLinkedList(concat(clickStream.getClicks(), anotherClickStream.getClicks())));
    }

    private static boolean isUserNameIsNotSet(Clickstream2 clickStream, Clickstream2 anotherClickStream) {
        return String.valueOf(clickStream.getUserName()).equals(USER_NOT_FOUND)
            && !(String.valueOf(clickStream.getUserName()).equals(anotherClickStream.getUserName()));
    }

    private static KStream<String, ValidationClickStream2> addModelToClickStreams(GlobalKTable<String, UserModel2> userModels,
                                                                                  KStream<String, Clickstream2> stringValidationClickStreamKStream) {
        KStream<String, ValidationClickStream2> stringRVKStream = stringValidationClickStreamKStream.leftJoin(
            userModels,
            (s, clickStream) -> String.valueOf(clickStream.getUserName()),
            (clickStream, userModel) -> {
                ValidationClickStream2 newClickStream = new ValidationClickStream2();
                newClickStream.setClicks(clickStream.getClicks());
                newClickStream.setSessionUUID(clickStream.getSessionUUID());
                newClickStream.setUserName(clickStream.getUserName());
                System.out.println("addUserModel: " + String.valueOf(Instant.now()
                    .toEpochMilli() - timeStampOfLastClick(clickStream)));
                newClickStream.setUserModel(userModel);
                return newClickStream;
            });
        return stringRVKStream;
    }

}
