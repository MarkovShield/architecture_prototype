package ch.hsr.markovshield.flink;

import ch.hsr.markovshield.models.Click;
import ch.hsr.markovshield.models.ClickStream;
import ch.hsr.markovshield.models.ClickStreamValidation;
import ch.hsr.markovshield.models.MarkovRating;
import ch.hsr.markovshield.models.ValidatedClickStream;
import ch.hsr.markovshield.models.ValidationClickStream;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.timestamps.AscendingTimestampExtractor;
import org.apache.flink.streaming.api.windowing.assigners.EventTimeSessionWindows;
import org.apache.flink.streaming.api.windowing.assigners.ProcessingTimeSessionWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
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
    public static final String MARKOV_VALIDATED_CLICK_STREAMS = "MarkovValidatedClickStream";
    public static final String FLINK_JOB_NAME = "Read from kafka and deserialize";
    public static final String REDIS_HOST = "redis";

    public static void main(final String[] args) throws Exception {

        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime);
        Properties properties = new Properties();
        properties.setProperty("bootstrap.servers", BROKER);
        properties.setProperty("zookeeper.connect", ZOOKEEPER);
        properties.setProperty("group.id", KAFKA_JOB_NAME);

        DataStreamSource<ValidationClickStream> stream = env
            .addSource(new FlinkKafkaConsumer010<>(MARKOV_CLICK_STREAM_ANALYSIS_TOPIC,
                new ValidationClickStreamDeserializationSchema(),
                properties));

        SingleOutputStreamOperator<ValidatedClickStream> validationStream = stream.map(MarkovShieldAnalyser::validateSession);
        RedisSink<ClickStreamValidation> sinkFunction = getRedisClickStreamValidationSink();
        validationStream.map(ValidatedClickStream::getClickStreamValidation)
            .filter(clickStreamValidation -> clickStreamValidation.getRating() != MarkovRating.UNEVALUDATED)
            .addSink(sinkFunction);


        SingleOutputStreamOperator<ValidatedClickStream> reduce = validationStream.assignTimestampsAndWatermarks(new AscendingTimestampExtractor<ValidatedClickStream>() {
            @Override
            public long extractAscendingTimestamp(ValidatedClickStream validatedClickStream) {
                return validatedClickStream.timeStampOfLastClick().getTime();
            }
        }).keyBy(ClickStream::getSessionUUID)
            .window(
                EventTimeSessionWindows.withGap(Time.minutes(2)))
            .reduce((clickStreamValidation, t1) -> {
                if (t1.getClickStreamValidation()
                    .getRating() == MarkovRating.UNEVALUDATED && clickStreamValidation.getClickStreamValidation()
                    .getRating() != MarkovRating.UNEVALUDATED) {
                    return new ValidatedClickStream(t1.getUserName(),
                        t1.getSessionUUID(),
                        t1.getClicks(),
                        clickStreamValidation.getClickStreamValidation());
                } else {
                    return t1;
                }
            });
        FlinkKafkaProducer010<ValidatedClickStream> producer = getKafkaValidatedClickStreamProducer();
        reduce.addSink(producer);


        env.execute(FLINK_JOB_NAME);
    }

    private static RedisSink<ClickStreamValidation> getRedisClickStreamValidationSink() {
        RedisMapper<ClickStreamValidation> redisMapper = new ClickStreamValidationRedisMapper();
        FlinkJedisPoolConfig conf = new FlinkJedisPoolConfig.Builder().setHost(REDIS_HOST).build();
        return new RedisSink<>(conf, redisMapper);
    }

    private static FlinkKafkaProducer010<ValidatedClickStream> getKafkaValidatedClickStreamProducer() {
        return new FlinkKafkaProducer010<>(
            "broker:9092",
            MARKOV_VALIDATED_CLICK_STREAMS,
            new ValidatedClickStreamSerializationSchema(MARKOV_VALIDATED_CLICK_STREAMS));
    }

    private static ValidatedClickStream validateSession(ValidationClickStream clickStream) {
        if (clickStream.lastClick().map(Click::isValidationRequired).orElse(false)) {
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
            ClickStreamValidation clickStreamValidation = new ClickStreamValidation(clickStream.getUserName(),
                clickStream.getSessionUUID(),
                clickStream.lastClick().map(Click::getClickUUID).orElse(null),
                score,
                rating);
            return new ValidatedClickStream(clickStream.getUserName(),
                clickStream.getSessionUUID(),
                clickStream.getClicks(),
                clickStreamValidation);

        } else {
            return new ValidatedClickStream(clickStream.getUserName(),
                clickStream.getSessionUUID(),
                clickStream.getClicks());
        }

    }

    private static MarkovRating calculateMarkovFraudLevel(int rating) {
        if (rating < 100) {
            return MarkovRating.OK;
        }
        if (rating < 150) {
            return MarkovRating.SUSPICIOUS;
        }
        return MarkovRating.FRAUD;
    }

}

