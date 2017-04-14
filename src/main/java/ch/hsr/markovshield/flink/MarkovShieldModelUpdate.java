package ch.hsr.markovshield.flink;

import ch.hsr.markovshield.models.ValidationClickStream;
import ch.hsr.markovshield.models.FrequencyModel;
import ch.hsr.markovshield.models.TransitionModel;
import ch.hsr.markovshield.models.UserModel;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.java.typeutils.TypeExtractor;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.datastream.WindowedStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer010;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaProducer010;
import org.apache.flink.streaming.util.serialization.KeyedDeserializationSchema;
import org.apache.flink.streaming.util.serialization.KeyedSerializationSchema;
import org.apache.flink.util.Collector;
import java.io.IOException;
import java.util.Properties;


public class MarkovShieldModelUpdate {

    public static final int REEVALUATION_INTERVAL_MINUTES = 5;
    public static final int SLIDING_TIME_MINUTES = 100;
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


        DataStreamSource<ValidationClickStream> stream = env
            .addSource(new FlinkKafkaConsumer010<ValidationClickStream>(MARKOV_CLICK_STREAM_TOPIC,
                new KeyedDeserializationSchema<ValidationClickStream>() {
                    @Override
                    public TypeInformation<ValidationClickStream> getProducedType() {
                        return TypeExtractor.getForClass(ValidationClickStream.class);
                    }

                    @Override
                    public ValidationClickStream deserialize(byte[] bytes, byte[] bytes1, String s, int i, long l) throws IOException {
                        ObjectMapper mapper = new ObjectMapper();
                        return mapper.readValue(bytes1, ValidationClickStream.class);
                    }

                    @Override
                    public boolean isEndOfStream(ValidationClickStream o) {

                        return false;
                    }
                },
                properties));
        WindowedStream<ValidationClickStream, String, TimeWindow> windowedStream = stream
            .keyBy(ValidationClickStream::getUserName)
            .timeWindow(Time.minutes(SLIDING_TIME_MINUTES), Time.minutes(REEVALUATION_INTERVAL_MINUTES));

        SingleOutputStreamOperator<UserModel> userModelStream = windowedStream.apply(MarkovShieldModelUpdate::recreateUserModel);

        FlinkKafkaProducer010<UserModel> producer = new FlinkKafkaProducer010<UserModel>(
            BROKER,
            MARKOV_USER_MODELS_TOPIC,
            new KeyedSerializationSchema<UserModel>() {

                @Override
                public byte[] serializeKey(UserModel model) {
                    return model.getUserId().getBytes();
                }

                @Override
                public byte[] serializeValue(UserModel model) {
                    try {
                        ObjectMapper mapper = new ObjectMapper();
                        return mapper.writeValueAsBytes(model);
                    } catch (JsonProcessingException e) {
                        e.printStackTrace();
                    }
                    return new byte[1];
                }

                @Override
                public String getTargetTopic(UserModel validation) {
                    return MARKOV_USER_MODELS_TOPIC;
                }
            });
        userModelStream.print();
        userModelStream.addSink(producer);

        env.execute(FLINK_JOB_NAME);
    }

    private static void recreateUserModel(String key, TimeWindow timeWindow, Iterable<ValidationClickStream> iterable, Collector<UserModel> collector) {
        UserModel model = null;
        for (ValidationClickStream clickStream : iterable) {
            if (model == null) {
                model = new UserModel(key, new TransitionModel(), new FrequencyModel());
            } else {
                if (!model.getUserId().equals(clickStream.getUserName())) {
                    throw new RuntimeException("UserName not the same");
                }
            }
        }
        collector.collect(model);

    }

}

