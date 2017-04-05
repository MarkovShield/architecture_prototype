package ch.hsr.markovshield.flink;

import ch.hsr.markovshield.models.ClickStream;
import ch.hsr.markovshield.models.ClickStreamValidation;
import ch.hsr.markovshield.models.MarkovRating;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.java.typeutils.TypeExtractor;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer010;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaProducer010;
import org.apache.flink.streaming.util.serialization.KeyedDeserializationSchema;
import org.apache.flink.streaming.util.serialization.KeyedSerializationSchema;
import java.io.IOException;
import java.util.Properties;


public class MarkovShieldAnalyser {

    public static final String BROKER = "broker:9092";
    public static final String ZOOKEEPER = "zookeeper:2181";
    public static final String KAFKA_JOB_NAME = "MarkovShieldAnalyser";
    public static final String MARKOV_CLICK_STREAM_TOPIC = "MarkovClickStreams";
    public static final String MARKOV_CLICK_STREAM_VALIDATION_TOPIC = "MarkovClickStreamValidations";
    public static final String FLINK_JOB_NAME = "Read from kafka and deserialize";

    public static void main(final String[] args) throws Exception {

        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();


        Properties properties = new Properties();
        properties.setProperty("bootstrap.servers", BROKER);
        properties.setProperty("zookeeper.connect", ZOOKEEPER);
        properties.setProperty("group.id", KAFKA_JOB_NAME);


        DataStreamSource<ClickStream> stream = env
            .addSource(new FlinkKafkaConsumer010<ClickStream>(MARKOV_CLICK_STREAM_TOPIC,
                new KeyedDeserializationSchema<ClickStream>() {
                    @Override
                    public TypeInformation<ClickStream> getProducedType() {
                        return TypeExtractor.getForClass(ClickStream.class);
                    }

                    @Override
                    public ClickStream deserialize(byte[] bytes, byte[] bytes1, String s, int i, long l) throws IOException {
                        ObjectMapper mapper = new ObjectMapper();
                        return mapper.readValue(bytes1, ClickStream.class);
                    }

                    @Override
                    public boolean isEndOfStream(ClickStream o) {

                        return false;
                    }
                },
                properties));


        SingleOutputStreamOperator<ClickStreamValidation> validationStream = stream.map(MarkovShieldAnalyser::validateSession);


        FlinkKafkaProducer010<ClickStreamValidation> producer = new FlinkKafkaProducer010<ClickStreamValidation>(
            "broker:9092",
            MARKOV_CLICK_STREAM_VALIDATION_TOPIC,
            new KeyedSerializationSchema<ClickStreamValidation>() {

                @Override
                public byte[] serializeKey(ClickStreamValidation validation) {
                    return validation.getSessionId().getBytes();
                }

                @Override
                public byte[] serializeValue(ClickStreamValidation validation) {
                    try {
                        ObjectMapper mapper = new ObjectMapper();
                        return mapper.writeValueAsBytes(validation);
                    } catch (JsonProcessingException e) {
                        e.printStackTrace();
                    }
                    return new byte[1];
                }

                @Override
                public String getTargetTopic(ClickStreamValidation validation) {
                    return MARKOV_CLICK_STREAM_VALIDATION_TOPIC;
                }
            });
        validationStream.addSink(producer);


        env.execute(FLINK_JOB_NAME);
    }

    private static ClickStreamValidation validateSession(ClickStream clickStream) {
        int score = 0;
        if (clickStream.getUserModel() != null) {
            score = clickStream.getUserModel().getFrequencyModel().getFrequencyValue() + clickStream.getUserModel()
                .getTransitionModel()
                .getTransitionValue();
        }
        MarkovRating rating = calculateMarkovFraudLevel(score);
        return new ClickStreamValidation(clickStream.getUserName(), clickStream.getSessionId(), score, rating);
    }

    private static MarkovRating calculateMarkovFraudLevel(int rating) {
        if (rating < 100) {
            return MarkovRating.VALID;
        }
        if (rating < 150) {
            return MarkovRating.SUSPICIOUS;
        }
        return MarkovRating.FRAUD;
    }

}

