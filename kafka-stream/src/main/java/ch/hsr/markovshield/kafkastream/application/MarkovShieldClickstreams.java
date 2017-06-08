package ch.hsr.markovshield.kafkastream.application;

import ch.hsr.markovshield.constants.MarkovTopics;
import ch.hsr.markovshield.kafkastream.streaming.MarkovClickStreamProcessing;
import ch.hsr.markovshield.kafkastream.streaming.StreamProcessing;
import ch.hsr.markovshield.utils.OptionHelper;
import io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.KStreamBuilder;
import org.apache.kafka.streams.processor.WallclockTimestampExtractor;
import org.apache.kafka.streams.state.HostInfo;
import java.util.Map;
import java.util.Properties;

import static ch.hsr.markovshield.constants.KafkaConnectionDefaults.DEFAULT_BOOTSTRAP_SERVERS;
import static ch.hsr.markovshield.constants.KafkaConnectionDefaults.DEFAULT_SCHEMA_REGISTRY_URL;
import static ch.hsr.markovshield.constants.KafkaConnectionDefaults.DEFAULT_ZOOKEEPER;

public class MarkovShieldClickstreams {

    public static final String KAFKA_JOB_NAME = "MarkovShieldClickstreams2";
    public static final String NUMBER_OF_THREADS_ARGUMENT = "numthreads";
    public static final String NUMBER_OF_THREADS_DEFAULT = "1";
    private static final String DEFAULT_REST_ENDPOINT_HOSTNAME = "localhost";
    private static final int DEFAULT_REST_ENDPOINT_PORT = 7777;
    private static final String SCHEMA_REGISTRY_ARGUMENT = "schemaregistry";
    private static final String RESTHOSTNAME_ARGUMENT = "resthostname";
    private static final String RESTPORT_ARGUMENT = "restport";

    public static void main(final String[] args) throws Exception {
        Options options = getOptions();
        OptionHelper.displayHelpOrExecute(options, args,
            commandLineArguments -> {
                try {
                    executeClickStream(commandLineArguments);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

    }

    private static void executeClickStream(CommandLine commandLineArguments) throws Exception {
        final HostInfo restEndpoint = parseHostInformation(commandLineArguments);
        final Properties streamsConfiguration = getStreamConfiguration(restEndpoint, commandLineArguments);
        setUpKafka(streamsConfiguration);
        StreamProcessing streamProcessing = new MarkovClickStreamProcessing();
        KStreamBuilder streamBuilder = streamProcessing.getStreamBuilder();
        StreamingApplication streamingApplication = new StreamingApplication(streamsConfiguration, streamBuilder);
        streamingApplication.startStreamingApp(restEndpoint);
    }

    private static HostInfo parseHostInformation(CommandLine cmd) {
        final int restEndpointPort = OptionHelper.getOption(cmd, RESTPORT_ARGUMENT).map(s -> Integer.valueOf(s))
            .orElse(DEFAULT_REST_ENDPOINT_PORT);
        final String restEndpointHostname = OptionHelper.getOption(cmd, RESTHOSTNAME_ARGUMENT)
            .orElse(DEFAULT_REST_ENDPOINT_HOSTNAME);
        return new HostInfo(restEndpointHostname, restEndpointPort);
    }

    private static void setUpKafka(Properties streamsConfiguration) {
        Properties topicCreatorProperties = (Properties) streamsConfiguration.clone();
        topicCreatorProperties.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        topicCreatorProperties.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        KafkaConsumer consumer = new KafkaConsumer(topicCreatorProperties);
        Map map = consumer.listTopics();
        KafkaTopicCreator kafkaTopicCreator = new KafkaTopicCreator(DEFAULT_ZOOKEEPER);
        Properties validatedClickStreamProperties = new Properties();
        validatedClickStreamProperties.put("cleanup.policy", "compact,delete");
        long sixMonthsInMs = 6 * 30 * 24 * 60 * 60 * 1000L;
        validatedClickStreamProperties.put("retention.ms", sixMonthsInMs);

        Properties userModelProperties = new Properties();
        userModelProperties.put("cleanup.policy", "compact,delete");
        userModelProperties.put("retention.ms", sixMonthsInMs);

        createTopicIfNotPresent(map, kafkaTopicCreator, MarkovTopics.MARKOV_CLICK_STREAM_ANALYSIS_TOPIC);
        createTopicIfNotPresent(map, kafkaTopicCreator, MarkovTopics.MARKOV_CLICK_STREAM_TOPIC);
        createTopicIfNotPresent(map, kafkaTopicCreator, MarkovTopics.MARKOV_LOGIN_TOPIC);
        createTopicIfNotPresent(map, kafkaTopicCreator, MarkovTopics.MARKOV_USER_MODEL_TOPIC, userModelProperties);
        createTopicIfNotPresent(map, kafkaTopicCreator, MarkovTopics.MARKOV_CLICK_TOPIC);
        createTopicIfNotPresent(map,
            kafkaTopicCreator,
            MarkovTopics.MARKOV_VALIDATED_CLICK_STREAMS,
            validatedClickStreamProperties);
        kafkaTopicCreator.closeConnection();
    }

    private static void createTopicIfNotPresent(Map map,
                                                KafkaTopicCreator kafkaTopicCreator,
                                                String topic,
                                                Properties topicConfig) {
        if (!map.containsKey(topic)) {
            kafkaTopicCreator.createTopic(topic, topicConfig);
        }
    }

    private static void createTopicIfNotPresent(Map map, KafkaTopicCreator kafkaTopicCreator, String topic) {
        if (!map.containsKey(topic)) {
            kafkaTopicCreator.createTopic(topic);
        }
    }

    private static Properties getStreamConfiguration(HostInfo restEndpoint, CommandLine cmd) {
        final String bootstrapServers = OptionHelper.getOption(cmd, OptionHelper.BOOTSTRAP_ARGUMENT_NAME)
            .orElse(DEFAULT_BOOTSTRAP_SERVERS);
        final String schemaRegistryUrl = OptionHelper.getOption(cmd, SCHEMA_REGISTRY_ARGUMENT)
            .orElse(DEFAULT_SCHEMA_REGISTRY_URL);
        final String numberOfThreads = OptionHelper.getOption(cmd, NUMBER_OF_THREADS_ARGUMENT).orElse(
            NUMBER_OF_THREADS_DEFAULT);
        final Serde<String> stringSerde = Serdes.String();
        final Properties streamsConfiguration = new Properties();
        streamsConfiguration.put(StreamsConfig.APPLICATION_ID_CONFIG, KAFKA_JOB_NAME);
        streamsConfiguration.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        streamsConfiguration.put(AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, schemaRegistryUrl);
        streamsConfiguration.put(StreamsConfig.KEY_SERDE_CLASS_CONFIG, stringSerde.getClass().getName());
        streamsConfiguration.put(StreamsConfig.VALUE_SERDE_CLASS_CONFIG, stringSerde.getClass().getName());
        streamsConfiguration.put(StreamsConfig.CACHE_MAX_BYTES_BUFFERING_CONFIG, 0);
        streamsConfiguration.put(StreamsConfig.ROCKSDB_CONFIG_SETTER_CLASS_CONFIG, CustomRocksDBConfig.class);
        streamsConfiguration.put(StreamsConfig.NUM_STREAM_THREADS_CONFIG, numberOfThreads);
        streamsConfiguration.put(StreamsConfig.APPLICATION_SERVER_CONFIG,
            restEndpoint.host() + ":" + restEndpoint.port());
        streamsConfiguration.put(StreamsConfig.STATE_DIR_CONFIG, "kafka-store");
        streamsConfiguration.put(StreamsConfig.TIMESTAMP_EXTRACTOR_CLASS_CONFIG, WallclockTimestampExtractor.class);
        return streamsConfiguration;
    }

    private static Options getOptions() {
        Options options = OptionHelper.getBasicKafkaOptions();
        Option schemaregistry = Option.builder()
            .longOpt(MarkovShieldClickstreams.SCHEMA_REGISTRY_ARGUMENT)
            .hasArg()
            .numberOfArgs(1)
            .desc("address of the SCHEMA_REGISTRY_ARGUMENT, it's default is: " + DEFAULT_SCHEMA_REGISTRY_URL)
            .build();
        Option resthostname = Option.builder()
            .longOpt(MarkovShieldClickstreams.RESTHOSTNAME_ARGUMENT)
            .hasArg()
            .numberOfArgs(1)
            .desc("port of the REST endpoint, it's default is: " + DEFAULT_REST_ENDPOINT_HOSTNAME)
            .build();
        Option restport = Option.builder()
            .longOpt(MarkovShieldClickstreams.RESTPORT_ARGUMENT)
            .hasArg()
            .numberOfArgs(1)
            .desc("hostname of the REST endpoint, it's default is: " + DEFAULT_REST_ENDPOINT_PORT)
            .build();
        Option numThreads = Option.builder()
            .longOpt(NUMBER_OF_THREADS_ARGUMENT)
            .hasArg()
            .numberOfArgs(1)
            .desc("the number of threads that kafka-streams will use, it's default is: " + NUMBER_OF_THREADS_DEFAULT)
            .build();
        options.addOption(schemaregistry);
        options.addOption(resthostname);
        options.addOption(restport);
        options.addOption(numThreads);
        return options;
    }
}


