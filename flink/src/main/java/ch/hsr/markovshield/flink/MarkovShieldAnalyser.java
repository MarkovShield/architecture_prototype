package ch.hsr.markovshield.flink;

import ch.hsr.markovshield.models.Click;
import ch.hsr.markovshield.models.ClickStreamValidation;
import ch.hsr.markovshield.models.MarkovRating;
import ch.hsr.markovshield.models.ValidationClickStream;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer010;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaProducer010;
import org.apache.flink.streaming.connectors.redis.RedisSink;
import org.apache.flink.streaming.connectors.redis.common.config.FlinkJedisPoolConfig;
import org.apache.flink.streaming.connectors.redis.common.mapper.RedisMapper;
import java.util.Properties;


public class MarkovShieldAnalyser {

    public static final String BROKER = "broker:9092";
    public static final String ZOOKEEPER = "zookeeper:2181";
    public static final String KAFKA_JOB_NAME = "MarkovShieldAnalyser";
    public static final String MARKOV_CLICK_STREAM_ANALYSIS_TOPIC = "MarkovClickStreamAnalysis";
    public static final String MARKOV_CLICK_STREAM_VALIDATION_TOPIC = "MarkovClickStreamValidations";
    public static final String FLINK_JOB_NAME = "Read from kafka and deserialize";
    public static final String REDIS_HOST = "redis";

    public static void main(final String[] args) throws Exception {

        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();


        Properties properties = new Properties();
        properties.setProperty("bootstrap.servers", BROKER);
        properties.setProperty("zookeeper.connect", ZOOKEEPER);
        properties.setProperty("group.id", KAFKA_JOB_NAME);

        DataStreamSource<ValidationClickStream> stream = env
            .addSource(new FlinkKafkaConsumer010<>(MARKOV_CLICK_STREAM_ANALYSIS_TOPIC,
                new ClickStreamValidationDeserializationSchema(),
                properties));

        SingleOutputStreamOperator<ClickStreamValidation> validationStream = stream.map(MarkovShieldAnalyser::validateSession);

        RedisSink<ClickStreamValidation> sinkFunction = getRedisClickStreamValidationSink();
        validationStream.addSink(sinkFunction);
        FlinkKafkaProducer010<ClickStreamValidation> producer = getKafkaClickStreamValidationProducer();
        validationStream.addSink(producer);


        env.execute(FLINK_JOB_NAME);
    }

    private static RedisSink<ClickStreamValidation> getRedisClickStreamValidationSink() {
        RedisMapper<ClickStreamValidation> redisMapper = new ClickStreamValidationRedisMapper();
        FlinkJedisPoolConfig conf = new FlinkJedisPoolConfig.Builder().setHost(REDIS_HOST).build();
        return new RedisSink<>(conf, redisMapper);
    }

    private static FlinkKafkaProducer010<ClickStreamValidation> getKafkaClickStreamValidationProducer() {
        return new FlinkKafkaProducer010<>(
            "broker:9092",
            MARKOV_CLICK_STREAM_VALIDATION_TOPIC,
            new ClickStreamValidationSerializationSchema());
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
        return new ClickStreamValidation(clickStream.getUserName(), clickStream.getSessionUUID(),
            clickStream.lastClick().map(Click::getClickUUID).orElse(null),
            score, rating);
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

