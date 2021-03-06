package ch.hsr.markovshield.kafkastream.streaming;

import ch.hsr.markovshield.constants.MarkovTopics;
import ch.hsr.markovshield.models.Click;
import ch.hsr.markovshield.models.ClickStream;
import ch.hsr.markovshield.models.Session;
import ch.hsr.markovshield.models.UserModel;
import ch.hsr.markovshield.models.ValidatedClickStream;
import ch.hsr.markovshield.models.ValidationClickStream;
import ch.hsr.markovshield.utils.JsonPOJOSerde;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.kstream.GlobalKTable;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KStreamBuilder;
import org.apache.kafka.streams.kstream.KTable;
import java.sql.Date;
import java.time.Instant;
import java.util.Collections;

import static ch.hsr.markovshield.constants.MarkovConstants.USER_NOT_FOUND;
import static ch.hsr.markovshield.constants.MarkovTopics.MARKOV_CLICK_STREAM_ANALYSIS_TOPIC;
import static ch.hsr.markovshield.constants.MarkovTopics.MARKOV_CLICK_TOPIC;
import static ch.hsr.markovshield.constants.MarkovTopics.MARKOV_LOGIN_TOPIC;
import static ch.hsr.markovshield.constants.MarkovTopics.MARKOV_USER_MODEL_TOPIC;
import static ch.hsr.markovshield.utils.JsonPOJOSerde.MARKOV_SHIELD_SMILE;
import static ch.hsr.markovshield.utils.JsonPOJOSerde.MOD_MSHIELD_SMILE;

public class MarkovClickStreamProcessing implements StreamProcessing {


    public static final Serde<String> stringSerde = Serdes.String();
    public static final JsonPOJOSerde<ValidationClickStream> validationClickStreamSerde = new JsonPOJOSerde<>(
        ValidationClickStream.class, MARKOV_SHIELD_SMILE);
    public static final JsonPOJOSerde<ValidatedClickStream> validatedClickStreamSerde = new JsonPOJOSerde<>(
        ValidatedClickStream.class, MARKOV_SHIELD_SMILE);
    public static final JsonPOJOSerde<Click> clickSerde = new JsonPOJOSerde<>(Click.class, MOD_MSHIELD_SMILE);
    public static final JsonPOJOSerde<Session> sessionSerde = new JsonPOJOSerde<>(Session.class, MOD_MSHIELD_SMILE);
    public static final JsonPOJOSerde<UserModel> userModelSerde = new JsonPOJOSerde<>(UserModel.class,
        MARKOV_SHIELD_SMILE);
    public static final JsonPOJOSerde<ClickStream> clickStreamSerde = new JsonPOJOSerde<>(ClickStream.class,
        MARKOV_SHIELD_SMILE);
    public static final String MARKOV_LOGIN_STORE = "MarkovLoginStore";
    public static final String MARKOV_USER_MODEL_STORE = "MarkovUserModelStore";
    public static final String MARKOV_VALIDATED_CLICKSTREAMS_STORE = "MarkovValidatedClickstreamsStore";

    private static ClickStream getInitialClickStream(Click click, Session session) {
        String newUserName = session != null ? session.getUserName() : USER_NOT_FOUND;
        return new ClickStream(newUserName, click.getSessionUUID(), Collections.singletonList(click));
    }

    private static ClickStream reduceClickStreams(ClickStream clickStream, ClickStream anotherClickStream) {
        if (isUserNameIsNotSet(clickStream, anotherClickStream)) {
            String userName = anotherClickStream.getUserName();
            clickStream.setUserName(userName);
        }
        clickStream.addToClicks(anotherClickStream.getClicks());
        return clickStream;
    }

    private static boolean isUserNameIsNotSet(ClickStream clickStream, ClickStream anotherClickStream) {
        return clickStream.getUserName().equals(USER_NOT_FOUND)
            && !(clickStream.getUserName().equals(anotherClickStream.getUserName()));
    }

    @Override
    public KStreamBuilder getStreamBuilder() {
        KStreamBuilder builder = new KStreamBuilder();

        final GlobalKTable<String, Session> sessions = getSessionTable(builder);
        final GlobalKTable<String, UserModel> userModels = getUserModelTable(builder);
        final KStream<String, Click> views = getClickStream(builder);

        KTable<String, ClickStream> clickstreams = aggregateClicks(sessions, views);

        KStream<String, ClickStream> stringValidationClickStreamKStream = clickstreams.toStream((s, clickStream) -> clickStream
            .getUserName());

        KStream<String, ValidationClickStream> clickStreamsWithModel = addModelToClickStreams(userModels,
            stringValidationClickStreamKStream);

        outputClickstreamsForAnalysis(clickStreamsWithModel);

        getValidatedClickstreams(builder);

        return builder;
    }

    private static void outputClickstreamsForAnalysis(KStream<String, ValidationClickStream> clickStreamsWithModel) {
        KStream<String, ValidationClickStream> stringValidationClickStreamKStream = clickStreamsWithModel.mapValues(
            validationClickStream -> {
                validationClickStream.setKafkaLeftDate(Date.from(Instant.now()));
                return validationClickStream;
            });
        stringValidationClickStreamKStream.to(stringSerde,
            validationClickStreamSerde,
            MARKOV_CLICK_STREAM_ANALYSIS_TOPIC);
    }

    private static KStream<String, ValidationClickStream> addModelToClickStreams(GlobalKTable<String, UserModel> userModels,
                                                                                 KStream<String, ClickStream> stringValidationClickStreamKStream) {
        KStream<String, ValidationClickStream> stringValidationClickStreamKStream1 = stringValidationClickStreamKStream.mapValues(
            ValidationClickStream::fromClickStream);
        KStream<String, ValidationClickStream> stringRVKStream = stringValidationClickStreamKStream1.leftJoin(userModels,
            (s, clickStream) -> clickStream.getUserName(),
            (clickStream, userModel) -> {
                clickStream.setUserModel(userModel);
                return clickStream;
            });
        return stringRVKStream;
    }

    private static KTable<String, ClickStream> aggregateClicks(GlobalKTable<String, Session> sessions,
                                                               KStream<String, Click> clicks) {
        return clicks.leftJoin(sessions,
            (s, click) -> click.getSessionUUID(),
            MarkovClickStreamProcessing::getInitialClickStream).groupByKey(stringSerde,
            clickStreamSerde).reduce(MarkovClickStreamProcessing::reduceClickStreams, "MarkovClickStreamAggregation");
    }

    private static KStream<String, Click> getClickStream(KStreamBuilder builder) {
        KStream<String, Click> stringClickKStream = builder
            .stream(stringSerde,
                clickSerde,
                MARKOV_CLICK_TOPIC)
            .mapValues(click -> {
                click.setKafkaFirstProcessedDate(Date.from(Instant.now()));
                return click;
            });
        return stringClickKStream;
    }

    private static GlobalKTable<String, UserModel> getUserModelTable(KStreamBuilder builder) {
        return builder
            .globalTable(stringSerde,
                userModelSerde,
                MARKOV_USER_MODEL_TOPIC,
                MARKOV_USER_MODEL_STORE);
    }

    private static KTable<String, ValidatedClickStream> getValidatedClickstreams(KStreamBuilder builder) {
        return builder.table(stringSerde,
            validatedClickStreamSerde,
            MarkovTopics.MARKOV_VALIDATED_CLICK_STREAMS,
            MARKOV_VALIDATED_CLICKSTREAMS_STORE);
    }

    private static GlobalKTable<String, Session> getSessionTable(KStreamBuilder builder) {
        return builder.globalTable(stringSerde,
            sessionSerde,
            MARKOV_LOGIN_TOPIC,
            MARKOV_LOGIN_STORE);
    }

}
