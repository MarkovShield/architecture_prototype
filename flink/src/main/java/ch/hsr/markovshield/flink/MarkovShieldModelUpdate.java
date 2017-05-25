package ch.hsr.markovshield.flink;

import ch.hsr.markovshield.constants.MarkovTopics;
import ch.hsr.markovshield.ml.IQRFrequencyAnalysis;
import ch.hsr.markovshield.ml.MarkovChainWithMatrix;
import ch.hsr.markovshield.models.ClickStream;
import ch.hsr.markovshield.models.MarkovRating;
import ch.hsr.markovshield.models.UserModel;
import ch.hsr.markovshield.models.ValidatedClickStream;
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

    public static final int REEVALUATION_INTERVAL_MINUTES = 5;
    public static final int SLIDING_TIME_MINUTES = 20;
    public static final String FLINK_JOB_NAME = "MarkovShieldModelUpdate";
    public static final String KAFKA_JOB_NAME = "MarkovShieldModelUpdate";

    public static void main(final String[] args) throws Exception {

        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setStreamTimeCharacteristic(TimeCharacteristic.ProcessingTime);
        KafkaConfigurationHelper config = new KafkaConfigurationHelper(KAFKA_JOB_NAME);

        DataStreamSource<ValidatedClickStream> stream = env
            .addSource(new FlinkKafkaConsumer010<ValidatedClickStream>(MarkovTopics.MARKOV_VALIDATED_CLICK_STREAMS,
                new ValidatedClickStreamDeserializationSchema(),
                config.getKafkaProperties()));

        SingleOutputStreamOperator<ValidatedClickStream> reduce = stream.keyBy(ClickStream::getSessionUUID)
            .window(
                ProcessingTimeSessionWindows.withGap(Time.minutes(2)))
            .reduce(ValidatedClickStreamHelper::foldValidationClickStream);
        WindowedStream<ValidatedClickStream, String, TimeWindow> windowedStream = reduce
            .keyBy(ClickStream::getUserName)
            .timeWindow(Time.minutes(SLIDING_TIME_MINUTES), Time.minutes(REEVALUATION_INTERVAL_MINUTES));

        SingleOutputStreamOperator<UserModel> userModelStream = windowedStream.apply(MarkovShieldModelUpdate::recreateUserModel);

        FlinkKafkaProducer010<UserModel> producer = new FlinkKafkaProducer010<UserModel>(
            config.getBroker(),
            MarkovTopics.MARKOV_USER_MODEL_TOPIC,
            new UserModelSerializationSchema(MarkovTopics.MARKOV_USER_MODEL_TOPIC));
        userModelStream.print();
        userModelStream.addSink(producer);

        env.execute(FLINK_JOB_NAME);
    }

    private static void recreateUserModel(String key, TimeWindow timeWindow, Iterable<ValidatedClickStream> iterable, Collector<UserModel> collector) {
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

