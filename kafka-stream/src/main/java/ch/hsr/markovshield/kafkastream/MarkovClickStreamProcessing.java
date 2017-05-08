package ch.hsr.markovshield.kafkastream;

import ch.hsr.markovshield.models.Click;
import ch.hsr.markovshield.models.ClickStream;
import ch.hsr.markovshield.models.Session;
import ch.hsr.markovshield.models.UserModel;
import ch.hsr.markovshield.models.ValidationClickStream;
import ch.hsr.markovshield.utils.JsonPOJOSerde;
import com.google.common.collect.Lists;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KStreamBuilder;
import org.apache.kafka.streams.kstream.KTable;
import java.util.Collections;

import static ch.hsr.markovshield.kafkastream.MarkovTopics.MARKOV_CLICK_STREAM_ANALYSIS_TOPIC;
import static ch.hsr.markovshield.kafkastream.MarkovTopics.MARKOV_CLICK_TOPIC;
import static ch.hsr.markovshield.kafkastream.MarkovTopics.MARKOV_LOGIN_TOPIC;
import static ch.hsr.markovshield.kafkastream.MarkovTopics.MARKOV_USER_MODEL_TOPIC;
import static com.google.common.collect.Iterables.concat;


public class MarkovClickStreamProcessing implements StreamProcessing {

    public static final String USER_NOT_FOUND = "--------------------NOT FOUND---------------------------";
    public static final Serde<String> stringSerde = Serdes.String();
    public static final JsonPOJOSerde<ValidationClickStream> validationClickStreamSerde = new JsonPOJOSerde<>(
        ValidationClickStream.class);
    public static final JsonPOJOSerde<Click> clickSerde = new JsonPOJOSerde<>(Click.class);
    public static final JsonPOJOSerde<Session> sessionSerde = new JsonPOJOSerde<>(Session.class);
    public static final JsonPOJOSerde<UserModel> userModelSerde = new JsonPOJOSerde<>(UserModel.class);
    public static final JsonPOJOSerde<ClickStream> clickStreamSerde = new JsonPOJOSerde<>(ClickStream.class);

    private static ClickStream getInitialClickStream(Click click, Session session) {
        String newUserName;
        if (session != null) {
            newUserName = session.getUserName();
        } else {
            newUserName = USER_NOT_FOUND;
        }
        return new ClickStream(newUserName, click.getSessionUUID(), Collections.singletonList(click));
    }

    private static ClickStream reduceClickStreams(ClickStream clickStream, ClickStream anotherClickStream) {
        String userName = clickStream.getUserName();
        if (isUserNameIsNotSet(clickStream, anotherClickStream)) {
            userName = anotherClickStream.getUserName();
        }
        return new ClickStream(userName,
            clickStream.getSessionUUID(),
            Lists.newLinkedList(concat(clickStream.getClicks(), anotherClickStream.getClicks())));
    }

    private static boolean isUserNameIsNotSet(ClickStream clickStream, ClickStream anotherClickStream) {
        return clickStream.getUserName().equals(USER_NOT_FOUND)
            && !(clickStream.getUserName().equals(anotherClickStream.getUserName()));
    }

    @Override
    public KStreamBuilder getStreamBuilder() {
        KStreamBuilder builder = new KStreamBuilder();

        final KTable<String, Session> sessions = getSessionTable(builder);
        final KTable<String, UserModel> userModels = getUserModelTable(builder);
        final KStream<String, Click> views = getClickStream(builder);

        KTable<String, ClickStream> clickstreams = aggregateClicks(sessions, views);

        KStream<String, ClickStream> stringValidationClickStreamKStream = clickstreams.toStream((s, clickStream) -> clickStream
            .getUserName());

        KStream<String, ValidationClickStream> clickStreamsWithModel = addModelToClickStreams(userModels,
            stringValidationClickStreamKStream);

        outputClickstreamsForAnalysis(clickStreamsWithModel);

        stringValidationClickStreamKStream.print();
        userModels.foreach((key, value) -> System.out.println("UserModel: " + key + " " + value.toString()));
        sessions.foreach((key, value) -> System.out.println("Session: " + key + " " + value.toString()));
        views.foreach((key, value) -> System.out.println("Click: " + key + " " + value.toString()));
        clickStreamsWithModel.print();

        return builder;
    }

    private static void outputClickstreamsForAnalysis(KStream<String, ValidationClickStream> clickStreamsWithModel) {
        clickStreamsWithModel
            .to(stringSerde,
                validationClickStreamSerde,
                MARKOV_CLICK_STREAM_ANALYSIS_TOPIC);
    }

    private static KStream<String, ValidationClickStream> addModelToClickStreams(KTable<String, UserModel> userModels, KStream<String, ClickStream> stringValidationClickStreamKStream) {
        return stringValidationClickStreamKStream
            .mapValues(ValidationClickStream::fromClickStream)
            .leftJoin(userModels,
                (ValidationClickStream clickStream, UserModel userModel) -> {
                    clickStream.setUserModel(userModel);
                    return clickStream;
                },
                stringSerde,
                validationClickStreamSerde);
    }

    private static KTable<String, ClickStream> aggregateClicks(KTable<String, Session> sessions, KStream<String, Click> clicks) {
        return clicks
            .leftJoin(sessions,
                MarkovClickStreamProcessing::getInitialClickStream,
                stringSerde,
                clickSerde)
            .groupByKey(stringSerde,
                clickStreamSerde)
            .reduce(
                MarkovClickStreamProcessing::reduceClickStreams, "MarkovClickStreamAggregation"
            );
    }

    private static KStream<String, Click> getClickStream(KStreamBuilder builder) {
        return builder
            .stream(stringSerde,
                clickSerde,
                MARKOV_CLICK_TOPIC);
    }

    private static KTable<String, UserModel> getUserModelTable(KStreamBuilder builder) {
        return builder
            .table(stringSerde,
                userModelSerde,
                MARKOV_USER_MODEL_TOPIC,
                "MarkovUserModelStore");
    }

    private static KTable<String, Session> getSessionTable(KStreamBuilder builder) {
        return builder
            .table(stringSerde,
                sessionSerde,
                MARKOV_LOGIN_TOPIC,
                "MarkovLoginStore");
    }

}
