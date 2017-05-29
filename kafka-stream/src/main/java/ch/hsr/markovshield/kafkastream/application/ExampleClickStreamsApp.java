package ch.hsr.markovshield.kafkastream.application;

import ch.hsr.markovshield.constants.MarkovTopics;
import ch.hsr.markovshield.models.Click;
import ch.hsr.markovshield.models.ClickStream;
import ch.hsr.markovshield.models.Session;
import ch.hsr.markovshield.models.UserModel;
import ch.hsr.markovshield.models.ValidatedClickStream;
import ch.hsr.markovshield.models.ValidationClickStream;
import ch.hsr.markovshield.utils.JsonPOJOSerde;
import com.google.common.collect.Lists;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.GlobalKTable;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KStreamBuilder;
import org.apache.kafka.streams.kstream.KTable;
import java.io.IOException;
import java.sql.Date;
import java.time.Instant;
import java.util.Collections;
import java.util.Properties;

import static ch.hsr.markovshield.constants.MarkovTopics.MARKOV_CLICK_STREAM_ANALYSIS_TOPIC;
import static ch.hsr.markovshield.constants.MarkovTopics.MARKOV_LOGIN_TOPIC;
import static ch.hsr.markovshield.constants.MarkovTopics.MARKOV_USER_MODEL_TOPIC;
import static ch.hsr.markovshield.kafkastream.streaming.MarkovClickStreamProcessing.MARKOV_LOGIN_STORE;
import static ch.hsr.markovshield.kafkastream.streaming.MarkovClickStreamProcessing.MARKOV_USER_MODEL_STORE;
import static ch.hsr.markovshield.kafkastream.streaming.MarkovClickStreamProcessing.USER_NOT_FOUND;
import static ch.hsr.markovshield.kafkastream.streaming.MarkovClickStreamProcessing.clickStreamSerde;
import static ch.hsr.markovshield.kafkastream.streaming.MarkovClickStreamProcessing.sessionSerde;
import static ch.hsr.markovshield.kafkastream.streaming.MarkovClickStreamProcessing.userModelSerde;
import static ch.hsr.markovshield.kafkastream.streaming.MarkovClickStreamProcessing.validatedClickStreamSerde;
import static ch.hsr.markovshield.kafkastream.streaming.MarkovClickStreamProcessing.validationClickStreamSerde;
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
        properties.put(StreamsConfig.COMMIT_INTERVAL_MS_CONFIG, commitInterval);
        properties.put(StreamsConfig.NUM_STREAM_THREADS_CONFIG, threads);
        properties.put(StreamsConfig.KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        properties.put(StreamsConfig.KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        properties.put(StreamsConfig.APPLICATION_ID_CONFIG, "ExampleClickStreams");

        KStreamBuilder builder = new KStreamBuilder();
        KStream<String, Click> markovClicks = builder
            .stream(stringSerde, clickSerde,
                "MarkovClicks");

      /*  markovClicks.mapValues(click -> {

            Instant l = Instant.now();
            click.setKafkaFirstProcessedDate(Date.from(l));
            return click;
        }).print();*/
        final GlobalKTable<String, Session> sessions = getSessionTable(builder);
        final GlobalKTable<String, UserModel> userModels = getUserModelTable(builder);

        KStream<String, ClickStream> markovClickStreamAggregation = markovClicks.groupByKey(stringSerde, clickSerde)
            .aggregate(() -> new ClickStream(USER_NOT_FOUND, null, Collections.emptyList()),
                ExampleClickStreamsApp::aggregateClick,
                clickStreamSerde,
                "MarkovClickStreamAggregation").toStream();
        KStream<String, ClickStream> clickStreamsWithSessions = addSessionToClickStream(sessions,
            markovClickStreamAggregation);
        clickStreamsWithSessions.mapValues(clickStream -> {
                System.out.println("reducedClickStreams: " + String.valueOf(Instant.now()
                    .toEpochMilli() - clickStream.getClicks().get(0).getKafkaFirstProcessedDate().toInstant().toEpochMilli()));
                return clickStream;
            }
        );
        KStream<String, ValidationClickStream> clickStreamsWithModel = addModelToClickStreams(userModels,
            clickStreamsWithSessions);
        outputClickstreamsForAnalysis(clickStreamsWithModel);
        getValidatedClickstreams(builder).mapValues(clickStream -> clickStream.timeStampOfLastClick()
            .toInstant()
            .toEpochMilli() - clickStream.getClickStreamValidation().getTimeCreated().toInstant().toEpochMilli());
        KafkaStreams streams = new KafkaStreams(builder, properties);
        streams.start();

        Runtime.getRuntime().addShutdownHook(new Thread(streams::close));

    }

    private static KStream<String, ClickStream> addSessionToClickStream(GlobalKTable<String, Session> sessions,
                                                                        KStream<String, ClickStream> markovClickStreamAggregation) {
        KStream<String, ClickStream> x = markovClickStreamAggregation.leftJoin(sessions,
            (s, clickStream) -> clickStream.getSessionUUID(),
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

    private static void outputClickstreamsForAnalysis(KStream<String, ValidationClickStream> clickStreamsWithModel) {
        clickStreamsWithModel.mapValues(validationClickStream -> {
            validationClickStream.setKafkaLeftDate(Date.from(Instant.now()));
            System.out.println("outputToKafka: " + String.valueOf(Instant.now()
                .toEpochMilli() - validationClickStream.timeStampOfLastClick().toInstant().toEpochMilli()));
            return validationClickStream;
        })
            .to(stringSerde,
                validationClickStreamSerde,
                MARKOV_CLICK_STREAM_ANALYSIS_TOPIC);
    }

    private static GlobalKTable<String, UserModel> getUserModelTable(KStreamBuilder builder) {
        return builder
            .globalTable(stringSerde,
                userModelSerde,
                MARKOV_USER_MODEL_TOPIC,
                MARKOV_USER_MODEL_STORE);
    }


    private static KTable<String, ClickStream> aggregateClicks(GlobalKTable<String, Session> sessions,
                                                               KStream<String, Click> clicks) {


        return clicks.leftJoin(sessions,
            (s, click) -> click.getSessionUUID(),
            ExampleClickStreamsApp::getInitialClickStream).groupByKey(stringSerde,
            clickStreamSerde).reduce(ExampleClickStreamsApp::reduceClickStreams, "MarkovClickStreamAggregation");
    }

    private static ClickStream aggregateClick(String sessionId, Click click, ClickStream clickStream) {
        if (clickStream.getSessionUUID() == null) {
            clickStream.setSessionUUID(click.getSessionUUID());
        }
        clickStream.addToClicks(click);
        System.out.println("reduceClickStreams: " + String.valueOf(Instant.now()
            .toEpochMilli() - clickStream.timeStampOfLastClick().toInstant().toEpochMilli()));
        return clickStream;

    }

    private static GlobalKTable<String, Session> getSessionTable(KStreamBuilder builder) {
        return builder.globalTable(stringSerde,
            sessionSerde,
            MARKOV_LOGIN_TOPIC,
            MARKOV_LOGIN_STORE);
    }

    private static ClickStream getInitialClickStream(Click click, Session session) {
        String newUserName = session != null ? session.getUserName() : USER_NOT_FOUND;
        return new ClickStream(newUserName, click.getSessionUUID(), Collections.singletonList(click));
    }

    private static ClickStream reduceClickStreams(ClickStream clickStream, ClickStream anotherClickStream) {
        String userName = clickStream.getUserName();
        if (isUserNameIsNotSet(clickStream, anotherClickStream)) {
            userName = anotherClickStream.getUserName();
        }
        System.out.println("reduceClickStreams: " + String.valueOf(Instant.now()
            .toEpochMilli() - clickStream.timeStampOfLastClick().toInstant().toEpochMilli()));
        return new ClickStream(userName,
            clickStream.getSessionUUID(),
            Lists.newLinkedList(concat(clickStream.getClicks(), anotherClickStream.getClicks())));
    }

    private static boolean isUserNameIsNotSet(ClickStream clickStream, ClickStream anotherClickStream) {
        return clickStream.getUserName().equals(USER_NOT_FOUND)
            && !(clickStream.getUserName().equals(anotherClickStream.getUserName()));
    }

    private static KStream<String, ValidationClickStream> addModelToClickStreams(GlobalKTable<String, UserModel> userModels,
                                                                                 KStream<String, ClickStream> stringValidationClickStreamKStream) {
        KStream<String, ValidationClickStream> stringValidationClickStreamKStream1 = stringValidationClickStreamKStream.mapValues(
            ValidationClickStream::fromClickStream);
        KStream<String, ValidationClickStream> stringRVKStream = stringValidationClickStreamKStream1.leftJoin(userModels,
            (s, clickStream) -> clickStream.getUserName(),
            (clickStream, userModel) -> {
                System.out.println("addUserModel: " + String.valueOf(Instant.now()
                    .toEpochMilli() - clickStream.timeStampOfLastClick().toInstant().toEpochMilli()));
                clickStream.setUserModel(userModel);
                return clickStream;
            });
        return stringRVKStream;
    }

}
