package ch.hsr.markovshield.flink;

import ch.hsr.markovshield.constants.MarkovTopics;
import ch.hsr.markovshield.models.Click;
import ch.hsr.markovshield.models.ClickStream;
import ch.hsr.markovshield.models.ClickStreamValidation;
import ch.hsr.markovshield.models.MarkovRating;
import ch.hsr.markovshield.models.UserModel;
import ch.hsr.markovshield.models.ValidatedClickStream;
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

    public static final String KAFKA_JOB_NAME = "MarkovShieldAnalyser";
    public static final String FLINK_JOB_NAME = "MarkovShieldAnalyser";
    public static final String REDIS_HOST = "redis";

    public static void main(final String[] args) throws Exception {

        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        KafkaConfigurationHelper kafkaConfigurationHelper = new KafkaConfigurationHelper(KAFKA_JOB_NAME);

        DataStreamSource<ValidationClickStream> stream = env
            .addSource(new FlinkKafkaConsumer010<>(MarkovTopics.MARKOV_CLICK_STREAM_ANALYSIS_TOPIC,
                new ValidationClickStreamDeserializationSchema(),
                kafkaConfigurationHelper.getKafkaProperties()));

        SingleOutputStreamOperator<ValidatedClickStream> validationStream = stream.map(MarkovShieldAnalyser::validateSession);
        RedisSink<ClickStreamValidation> sinkFunction = getRedisClickStreamValidationSink();
        validationStream.map(ValidatedClickStream::getClickStreamValidation)
            .filter(clickStreamValidation -> clickStreamValidation.getRating() != MarkovRating.UNEVALUDATED)
            .addSink(sinkFunction);


        SingleOutputStreamOperator<ValidatedClickStream> reduce = validationStream.keyBy(ClickStream::getSessionUUID)
            .fold(null, ValidatedClickStreamHelper::foldValidationClickStream);
        FlinkKafkaProducer010<ValidatedClickStream> producer = getKafkaValidatedClickStreamProducer(
            kafkaConfigurationHelper.getBroker());
        reduce.addSink(producer);

        env.execute(FLINK_JOB_NAME);
    }

    private static RedisSink<ClickStreamValidation> getRedisClickStreamValidationSink() {
        RedisMapper<ClickStreamValidation> redisMapper = new ClickStreamValidationRedisMapper();
        FlinkJedisPoolConfig conf = new FlinkJedisPoolConfig.Builder().setHost(REDIS_HOST).build();
        return new RedisSink<>(conf, redisMapper);
    }

    private static FlinkKafkaProducer010<ValidatedClickStream> getKafkaValidatedClickStreamProducer(String broker) {
        return new FlinkKafkaProducer010<>(
            broker,
            MarkovTopics.MARKOV_VALIDATED_CLICK_STREAMS,
            new ValidatedClickStreamSerializationSchema(MarkovTopics.MARKOV_VALIDATED_CLICK_STREAMS));
    }

    private static ValidatedClickStream validateSession(ValidationClickStream clickStream) {
        if (clickStream.lastClick().map(Click::isValidationRequired).orElse(false)) {
            double score = 0;
            UserModel userModel = clickStream.getUserModel();
            if (userModel != null) {
                score = userModel.getClickStreamModels()
                    .stream()
                    .mapToDouble(clickStreamModel -> clickStreamModel.clickStreamScore(clickStream))
                    .sum();
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

    private static MarkovRating calculateMarkovFraudLevel(double rating) {
        if (rating < 100) {
            return MarkovRating.OK;
        }
        if (rating < 150) {
            return MarkovRating.SUSPICIOUS;
        }
        return MarkovRating.FRAUD;
    }

}

