package ch.hsr.markovshield.flink;

import ch.hsr.markovshield.ml.MarkovChainWithMatrix;
import ch.hsr.markovshield.models.Click;
import ch.hsr.markovshield.models.ClickStream;
import ch.hsr.markovshield.models.ClickStreamValidation;
import ch.hsr.markovshield.models.MarkovRating;
import ch.hsr.markovshield.models.MatrixFrequencyModel;
import ch.hsr.markovshield.models.TransitionModel;
import ch.hsr.markovshield.models.UrlFrequencies;
import ch.hsr.markovshield.models.ValidatedClickStream;
import ch.hsr.markovshield.models.ValidationClickStream;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.assigners.ProcessingTimeSessionWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer010;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaProducer010;
import org.apache.flink.streaming.connectors.redis.RedisSink;
import org.apache.flink.streaming.connectors.redis.common.config.FlinkJedisPoolConfig;
import org.apache.flink.streaming.connectors.redis.common.mapper.RedisMapper;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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


        SingleOutputStreamOperator<ValidatedClickStream> reduce = validationStream.keyBy(ClickStream::getSessionUUID)
            .window(
                ProcessingTimeSessionWindows.withGap(Time.minutes(2)))
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
            double score = 0;
            if (clickStream.getUserModel() != null) {
                double frequencyValue = getFrequencyScore(clickStream);
                double transitionValue = getTransitionScore(clickStream);
                score = frequencyValue + transitionValue;
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

    private static double getTransitionScore(ValidationClickStream clickStream) {
        double transitionScore = 0;
        TransitionModel transitionModel = clickStream.getUserModel().getTransitionModel();
        List<Click> clicks = clickStream.getClicks();
        for (int i = 0; i < clicks.size(); i++) {
            double probabilityForClick;
            if (i == clicks.size() - 1) {
                probabilityForClick = transitionModel.getProbabilityForClick(clicks.get(i).getUrl(),
                    MarkovChainWithMatrix.END_OF_CLICK_STREAM);
            } else {
                probabilityForClick = transitionModel.getProbabilityForClick(clicks.get(i), clicks.get(i + 1));
            }
            transitionScore += (1 - probabilityForClick) * clicks.get(i).getUrlRiskLevel();
        }
        return transitionScore;
    }

    private static double getFrequencyScore(ValidationClickStream clickStream) {
        double frequencyScore = 0;
        HashMap<String, UrlFrequencies> frequencies = getFrequencies(clickStream);
        MatrixFrequencyModel frequencyModel = clickStream.getUserModel()
            .getFrequencyModel();
        for (Map.Entry<String, UrlFrequencies> entry : frequencies.entrySet()
            ) {
            Integer currentFrequencies = entry.getValue().getFrequencyCounter();
            String currentUrl = entry.getKey();
            if (frequencyModel.getFrequencyLowerBound(currentUrl) < currentFrequencies || frequencyModel.getFrequencyUpperBound(
                currentUrl) > currentFrequencies) {
                frequencyScore += entry.getValue().getUrlRiskLevel() + 1;
            }
        }
        return frequencyScore;
    }

    private static HashMap<String, UrlFrequencies> getFrequencies(ValidationClickStream clickStream) {
        HashMap<String, UrlFrequencies> frequencyMap = new HashMap<>();
        for (Click click : clickStream.getClicks()) {
            String url = click.getUrl();
            if (frequencyMap.containsKey(url)) {
                frequencyMap.get(url).increaseFrequencyCounter();
            } else {
                frequencyMap.put(url, new UrlFrequencies(url, 1, click.getUrlRiskLevel()));
            }
        }
        return frequencyMap;
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

