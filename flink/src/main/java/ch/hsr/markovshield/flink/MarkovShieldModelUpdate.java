package ch.hsr.markovshield.flink;

import ch.hsr.markovshield.constants.MarkovTopics;
import ch.hsr.markovshield.ml.IQRFrequencyAnalysis;
import ch.hsr.markovshield.ml.MarkovChainWithMatrix;
import ch.hsr.markovshield.models.ClickStream;
import ch.hsr.markovshield.models.MarkovRating;
import ch.hsr.markovshield.models.UserModel;
import ch.hsr.markovshield.models.ValidatedClickStream;
import ch.hsr.markovshield.utils.OptionHelper;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
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
import java.util.ArrayList;
import java.util.List;

public class MarkovShieldModelUpdate {

    public static final int DEFAULT_REEVALUATION_INTERVAL_MINUTES = 1440;
    public static final int DEFAULT_SLIDING_TIME_MINUTES = 260000;
    public static final String FLINK_JOB_NAME = "MarkovShieldModelUpdate";
    public static final String KAFKA_JOB_NAME = "MarkovShieldModelUpdate";
    private static final String LOOKBACKPERIOD_ARGUMENT_NAME = "lookbackperiod";
    private static final String UPDATEINTERVAL_ARGUMENT_NAME = "updateinterval";
    private static final String SESSION_TIMEOUT_ARGUMENT_NAME = "sessiontimeout";
    private static final int DEFAULT_SESSION_TIME_MINUTES = 60;

    public static void main(final String[] args) throws Exception {
        Options options = getOptions();
        OptionHelper.displayHelpOrExecute(options, args,
            commandLineArguments -> {
                try {
                    executeModelUpdate(commandLineArguments);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

    }

    private static void executeModelUpdate(CommandLine commandLineArguments) throws Exception {
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

        SingleOutputStreamOperator<UserModel> userModelStream = windowedStream.apply(MarkovShieldModelUpdate::recreateUserModel);

        FlinkKafkaProducer010<UserModel> producer = new FlinkKafkaProducer010<>(
            config.getBroker(),
            MarkovTopics.MARKOV_USER_MODEL_TOPIC,
            new UserModelSerializationSchema(MarkovTopics.MARKOV_USER_MODEL_TOPIC));
        userModelStream.print();
        userModelStream.addSink(producer);

        env.execute(FLINK_JOB_NAME);
    }

    public static Options getOptions() {

        Options options = OptionHelper.getBasicKafkaOptions();
        Option updatetime = Option.builder()
            .longOpt(UPDATEINTERVAL_ARGUMENT_NAME)
            .hasArg()
            .numberOfArgs(1)
            .desc("time between the update of the usermodels in minutes, it's default is:" + DEFAULT_REEVALUATION_INTERVAL_MINUTES)
            .build();
        Option slidingtime = Option.builder()
            .longOpt(LOOKBACKPERIOD_ARGUMENT_NAME)
            .hasArg()
            .numberOfArgs(1)
            .desc("lookbackperiod of the usermodels in minutes, it's default is:" + DEFAULT_SLIDING_TIME_MINUTES)
            .build();
        Option sessiontime = Option.builder()
            .longOpt(SESSION_TIMEOUT_ARGUMENT_NAME)
            .hasArg()
            .numberOfArgs(1)
            .desc("session timeout in minutes, it's default is:" + DEFAULT_SESSION_TIME_MINUTES)
            .build();
        options.addOption(updatetime);
        options.addOption(slidingtime);
        options.addOption(sessiontime);
        return options;
    }

    private static void recreateUserModel(String key,
                                          TimeWindow timeWindow,
                                          Iterable<ValidatedClickStream> iterable,
                                          Collector<UserModel> collector) {
        List<ClickStream> filteredClicks = new ArrayList<>();
        for (ValidatedClickStream clickStream : iterable) {
            MarkovRating rating = clickStream.getClickStreamValidation().getRating();
            if (rating == MarkovRating.UNEVALUDATED || rating == MarkovRating.OK) {
                filteredClicks.add(clickStream);
            }
        }

        IQRFrequencyAnalysis frequencyAnalysis = new IQRFrequencyAnalysis();
        UserModel model = new UserModel(key,
            MarkovChainWithMatrix.train(filteredClicks),
            frequencyAnalysis.train(filteredClicks));
        collector.collect(model);

    }
}

