package ch.hsr.markovshield.flink;

import ch.hsr.markovshield.ml.FrequencyMatrix;
import ch.hsr.markovshield.ml.MarkovChainWithMatrix;
import ch.hsr.markovshield.models.ClickStream;
import ch.hsr.markovshield.models.MatrixFrequencyModel;
import ch.hsr.markovshield.models.MarkovRating;
import ch.hsr.markovshield.models.UrlStore;
import ch.hsr.markovshield.models.UserModel;
import ch.hsr.markovshield.models.ValidatedClickStream;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.datastream.WindowedStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer010;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaProducer010;
import org.apache.flink.util.Collector;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;


public class MarkovShieldModelUpdate {

    public static final int REEVALUATION_INTERVAL_MINUTES = 5;
    public static final int SLIDING_TIME_MINUTES = 20;
    public static final String MARKOV_CLICK_STREAM_TOPIC = "MarkovClickStreams";
    public static final String MARKOV_USER_MODELS_TOPIC = "MarkovUserModels";
    public static final String FLINK_JOB_NAME = "UpdateUserModels";
    public static final String BROKER = "broker:9092";
    public static final String ZOOKEEPER = "zookeeper:2181";
    public static final String KAFKA_JOB_NAME = "MarkovShieldModelUpdate";

    public static void main(final String[] args) throws Exception {

        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setStreamTimeCharacteristic(TimeCharacteristic.ProcessingTime);


        Properties properties = new Properties();
        properties.setProperty("bootstrap.servers", BROKER);
        properties.setProperty("zookeeper.connect", ZOOKEEPER);
        properties.setProperty("group.id", KAFKA_JOB_NAME);


        DataStreamSource<ValidatedClickStream> stream = env
            .addSource(new FlinkKafkaConsumer010<ValidatedClickStream>(MarkovShieldAnalyser.MARKOV_VALIDATED_CLICK_STREAMS,
                new ValidatedClickStreamDeserializationSchema(),
                properties));
        WindowedStream<ValidatedClickStream, String, TimeWindow> windowedStream = stream
            .keyBy(ClickStream::getUserName)
            .timeWindow(Time.minutes(SLIDING_TIME_MINUTES), Time.minutes(REEVALUATION_INTERVAL_MINUTES));

        SingleOutputStreamOperator<UserModel> userModelStream = windowedStream.apply(MarkovShieldModelUpdate::recreateUserModel);

        FlinkKafkaProducer010<UserModel> producer = new FlinkKafkaProducer010<UserModel>(
            BROKER,
            MARKOV_USER_MODELS_TOPIC,
            new UserModelSerializationSchema(MARKOV_USER_MODELS_TOPIC));
        userModelStream.print();
        userModelStream.addSink(producer);

        env.execute(FLINK_JOB_NAME);
    }

    private static void recreateUserModel(String key, TimeWindow timeWindow, Iterable<ValidatedClickStream> iterable, Collector<UserModel> collector) {
        UserModel model = null;
        List<ClickStream> filteredClicks = new ArrayList<>();

        for (ValidatedClickStream clickStream : iterable) {
            MarkovRating rating = clickStream.getClickStreamValidation().getRating();
            if (rating == MarkovRating.UNEVALUDATED || rating == MarkovRating.VALID) {
                filteredClicks.add(clickStream);
            }
        }
        //Do freqency Analysis


        FrequencyMatrix frequencyMatrix = null;
        UrlStore urlStore = null;
        model = new UserModel(key, MarkovChainWithMatrix.train(filteredClicks), new MatrixFrequencyModel(frequencyMatrix,
            urlStore));
        collector.collect(model);

    }

}

