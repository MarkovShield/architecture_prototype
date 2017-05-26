package ch.hsr.markovshield.flink;

import ch.hsr.markovshield.constants.MarkovTopics;
import ch.hsr.markovshield.models.Click;
import ch.hsr.markovshield.models.ClickStream;
import ch.hsr.markovshield.models.ClickStreamValidation;
import ch.hsr.markovshield.models.MarkovRating;
import ch.hsr.markovshield.models.UserModel;
import ch.hsr.markovshield.models.ValidatedClickStream;
import ch.hsr.markovshield.models.ValidationClickStream;
import ch.hsr.markovshield.utils.OptionHelper;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer010;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaProducer010;
import org.apache.flink.streaming.connectors.redis.RedisSink;
import org.apache.flink.streaming.connectors.redis.common.config.FlinkJedisPoolConfig;
import org.apache.flink.streaming.connectors.redis.common.mapper.RedisMapper;

public class MarkovShieldAnalyser {

    public static final String KAFKA_JOB_NAME = "MarkovShieldAnalyser";
    public static final String FLINK_JOB_NAME = "MarkovShieldAnalyser";
    public static final String DEFAULT_REDIS_HOST = "redis";
    private static final String REDIS_HOST_ARGUMENT_NAME = "redishost";
    private static final int DEFAULT_REDIS_PORT = 6379;
    private static final String REDIS_PORT_ARGUMENT_NAME = "redisport";

    public static void main(final String[] args) throws Exception {

        final Options options = getOptions();
        OptionHelper.displayHelpOrExecute(options, args,
            commandLineArguments -> {
                try {
                    executeAnalysis(commandLineArguments);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
    }


    private static void executeAnalysis(final CommandLine commandLineArguments) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        KafkaConfigurationHelper kafkaConfigurationHelper = new KafkaConfigurationHelper(KAFKA_JOB_NAME,
            commandLineArguments);

        DataStreamSource<ValidationClickStream> stream = env
            .addSource(new FlinkKafkaConsumer010<>(MarkovTopics.MARKOV_CLICK_STREAM_ANALYSIS_TOPIC,
                new ValidationClickStreamDeserializationSchema(),
                kafkaConfigurationHelper.getKafkaProperties()));

        SingleOutputStreamOperator<ValidatedClickStream> validationStream = stream.map(MarkovShieldAnalyser::validateSession);

        String redisHost = OptionHelper.getOption(commandLineArguments, REDIS_HOST_ARGUMENT_NAME)
            .orElse(DEFAULT_REDIS_HOST);
        Integer redisPort = OptionHelper.getOption(commandLineArguments, REDIS_PORT_ARGUMENT_NAME)
            .map(Integer::valueOf)
            .orElse(DEFAULT_REDIS_PORT);

        RedisSink<ClickStreamValidation> sinkFunction = getRedisClickStreamValidationSink(redisHost, redisPort);
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

    private static RedisSink<ClickStreamValidation> getRedisClickStreamValidationSink(final String redisHost, final int redisPort) {
        RedisMapper<ClickStreamValidation> redisMapper = new ClickStreamValidationRedisMapper();
        FlinkJedisPoolConfig conf = new FlinkJedisPoolConfig.Builder().setHost(redisHost).setPort(redisPort).build();
        return new RedisSink<>(conf, redisMapper);
    }

    private static FlinkKafkaProducer010<ValidatedClickStream> getKafkaValidatedClickStreamProducer(final String broker) {
        return new FlinkKafkaProducer010<>(
            broker,
            MarkovTopics.MARKOV_VALIDATED_CLICK_STREAMS,
            new ValidatedClickStreamSerializationSchema(MarkovTopics.MARKOV_VALIDATED_CLICK_STREAMS));
    }

    private static Options getOptions() {
        final Options options = OptionHelper.getBasicKafkaOptions();
        Option redisHost = Option.builder()
            .longOpt(REDIS_HOST_ARGUMENT_NAME)
            .hasArg()
            .numberOfArgs(1)
            .desc("the hostname of redis, it's default is:" + DEFAULT_REDIS_HOST)
            .build();
        Option redisPort = Option.builder()
            .longOpt(REDIS_PORT_ARGUMENT_NAME)
            .hasArg()
            .numberOfArgs(1)
            .desc("the port of redis, it's default is:" + DEFAULT_REDIS_PORT)
            .build();
        options.addOption(redisHost);
        options.addOption(redisPort);
        return options;
    }

    private static ValidatedClickStream validateSession(final ValidationClickStream clickStream) {
        if (clickStream.lastClick().map(Click::isValidationRequired).orElse(false)) {
            double score = 0;
            UserModel userModel = clickStream.getUserModel();
            if (userModel != null) {
                score = userModel.getClickStreamModels()
                    .stream()
                    .mapToDouble(clickStreamModel -> clickStreamModel.clickStreamScore(clickStream))
                    .average()
                    .orElse(0);
            }
            final MarkovRating rating = calculateMarkovFraudLevel(score);
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

    private static MarkovRating calculateMarkovFraudLevel(final double rating) {
        if (rating <= 50) {
            return MarkovRating.OK;
        }
        if (rating <= 75) {
            return MarkovRating.SUSPICIOUS;
        }
        return MarkovRating.FRAUD;
    }
}

