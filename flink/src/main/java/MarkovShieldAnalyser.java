package ch.hsr.markovshield.flink;

import ch.hsr.markovshield.models.Click;
import ch.hsr.markovshield.models.ClickStreamValidation;
import ch.hsr.markovshield.models.MarkovRating;
import ch.hsr.markovshield.models.ValidationClickStream;
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
    public static final String MARKOV_CLICK_STREAM_ANALYSIS_TOPIC = "MarkovClickStreamAnalysis";
    public static final String MARKOV_CLICK_STREAM_VALIDATION_TOPIC = "MarkovClickStreamValidations";
    public static final String FLINK_JOB_NAME = "Read from kafka and deserialize";

    public static void main(final String[] args) throws Exception {

        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();


        Properties properties = new Properties();
        properties.setProperty("bootstrap.servers", BROKER);
        properties.setProperty("zookeeper.connect", ZOOKEEPER);
        properties.setProperty("group.id", KAFKA_JOB_NAME);


        DataStreamSource<ValidationClickStream> stream = env
            .addSource(new FlinkKafkaConsumer010<>(MARKOV_CLICK_STREAM_ANALYSIS_TOPIC,
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


        SingleOutputStreamOperator<ClickStreamValidation> validationStream = stream.map(MarkovShieldAnalyser::validateSession);


        FlinkKafkaProducer010<ClickStreamValidation> producer = new FlinkKafkaProducer010<>(
            "broker:9092",
            MARKOV_CLICK_STREAM_VALIDATION_TOPIC,
            new KeyedSerializationSchema<ClickStreamValidation>() {

                @Override
                public byte[] serializeKey(ClickStreamValidation validation) {
                    return validation.getSessionUUID().getBytes();
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

    private static ClickStreamValidation validateSession(ValidationClickStream clickStream) {
        int weightingScore;
        if (clickStream.getClicks() != null) {
            Click lastClick = clickStream.getClicks().get(clickStream.getClicks().size() - 1);
            if (lastClick != null) {
                weightingScore = lastClick.getUrlRiskLevel();
            } else {
                weightingScore = 1000;
            }
        } else {
            weightingScore = 1000;

        }
        int score = 0;
        if (clickStream.getUserModel() != null) {
            int frequencyValue = clickStream.getUserModel().getFrequencyModel().getFrequencyValue();
            int transitionValue = (clickStream.getUserModel()
                .getTransitionModel() != null) ? 1 : 100;
            score = (frequencyValue + transitionValue) * weightingScore;

        }
        MarkovRating rating = calculateMarkovFraudLevel(score);
        return new ClickStreamValidation(clickStream.getUserName(), clickStream.getSessionUUID(), score, rating);
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

