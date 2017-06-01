package ch.hsr.markovshield.flink;

import ch.hsr.markovshield.constants.MarkovTopics;
import ch.hsr.markovshield.models.ClickStream;
import ch.hsr.markovshield.models.ClickStreamModel;
import ch.hsr.markovshield.models.MarkovRating;
import ch.hsr.markovshield.models.UserModel;
import ch.hsr.markovshield.models.UserModelFactory;
import ch.hsr.markovshield.models.ValidatedClickStream;
import ch.hsr.markovshield.utils.OptionHelper;
import org.apache.commons.cli.CommandLine;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.datastream.WindowedStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.assigners.ProcessingTimeSessionWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer010;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaProducer010;
import org.apache.flink.util.Collector;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import static ch.hsr.markovshield.flink.MarkovShieldModelUpdater.DEFAULT_REEVALUATION_INTERVAL_MINUTES;
import static ch.hsr.markovshield.flink.MarkovShieldModelUpdater.DEFAULT_SESSION_TIME_MINUTES;
import static ch.hsr.markovshield.flink.MarkovShieldModelUpdater.DEFAULT_SLIDING_TIME_MINUTES;
import static ch.hsr.markovshield.flink.MarkovShieldModelUpdater.FLINK_JOB_NAME;
import static ch.hsr.markovshield.flink.MarkovShieldModelUpdater.KAFKA_JOB_NAME;
import static ch.hsr.markovshield.flink.MarkovShieldModelUpdater.LOOKBACKPERIOD_ARGUMENT_NAME;
import static ch.hsr.markovshield.flink.MarkovShieldModelUpdater.SESSION_TIMEOUT_ARGUMENT_NAME;
import static ch.hsr.markovshield.flink.MarkovShieldModelUpdater.UPDATEINTERVAL_ARGUMENT_NAME;

public class MarkovShieldModelUpdate implements Serializable {

    private final UserModelFactory userModelFactory;

    public MarkovShieldModelUpdate(UserModelFactory userModelFactory) {
        this.userModelFactory = userModelFactory;
    }

    public void executeModelUpdate(CommandLine commandLineArguments) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setStreamTimeCharacteristic(TimeCharacteristic.ProcessingTime);
        KafkaConfigurationHelper config = new KafkaConfigurationHelper(KAFKA_JOB_NAME, commandLineArguments);

        DataStreamSource<ValidatedClickStream> stream = env
            .addSource(new FlinkKafkaConsumer010<>(MarkovTopics.MARKOV_VALIDATED_CLICK_STREAMS,
                new ValidatedClickStreamDeserializationSchema(),
                config.getKafkaProperties()));
        Integer sessiontimeout = OptionHelper.getOption(commandLineArguments, SESSION_TIMEOUT_ARGUMENT_NAME)
            .map(Integer::valueOf)
            .orElse(DEFAULT_SESSION_TIME_MINUTES);
        SingleOutputStreamOperator<ValidatedClickStream> reduce = stream.keyBy(ClickStream::getSessionUUID)
            .window(
                ProcessingTimeSessionWindows.withGap(Time.minutes(sessiontimeout)))
            .reduce(ValidatedClickStreamHelper::foldValidationClickStream);
        Integer slidingtime = OptionHelper.getOption(commandLineArguments, LOOKBACKPERIOD_ARGUMENT_NAME)
            .map(Integer::valueOf)
            .orElse(DEFAULT_SLIDING_TIME_MINUTES);
        int reevaluationIntervalMinutes = OptionHelper.getOption(commandLineArguments, UPDATEINTERVAL_ARGUMENT_NAME)
            .map(Integer::valueOf)
            .orElse(DEFAULT_REEVALUATION_INTERVAL_MINUTES);

        WindowedStream<ValidatedClickStream, String, TimeWindow> windowedStream = reduce
            .keyBy(ClickStream::getUserName)
            .timeWindow(Time.minutes(slidingtime), Time.minutes(reevaluationIntervalMinutes));

        SingleOutputStreamOperator<UserModel> userModelStream = windowedStream.apply(this::recreateUserModel);

        FlinkKafkaProducer010<UserModel> producer = new FlinkKafkaProducer010<>(
            config.getBroker(),
            MarkovTopics.MARKOV_USER_MODEL_TOPIC,
            new UserModelSerializationSchema(MarkovTopics.MARKOV_USER_MODEL_TOPIC));
        userModelStream.addSink(producer);
        env.execute(FLINK_JOB_NAME);
    }

    private void recreateUserModel(String userId,
                                   TimeWindow timeWindow,
                                   Iterable<ValidatedClickStream> iterable,
                                   Collector<UserModel> collector) {
        int count = 0;
        for (ValidatedClickStream validatedClickStream : iterable) {
            count++;
        }
        System.out.println("recreate usermodel for " + userId + "with " + count);
        List<ClickStream> filteredClicks = new ArrayList<>();
        for (ValidatedClickStream clickStream : iterable) {
            MarkovRating rating = clickStream.getClickStreamValidation().getRating();
            if (rating == MarkovRating.UNEVALUDATED || rating == MarkovRating.OK) {
                filteredClicks.add(clickStream);
            }
        }

        List<ClickStreamModel> clickStreamModels = userModelFactory.trainAllModels(filteredClicks);
        UserModel model = new UserModel(userId, clickStreamModels);
        System.out.println(model);
        collector.collect(model);

    }

}

