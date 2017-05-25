package ch.hsr.markovshield.kafkastream.application;

import ch.hsr.markovshield.constants.MarkovTopics;
import ch.hsr.markovshield.kafkastream.streaming.KafkaTopicCreator;
import ch.hsr.markovshield.kafkastream.streaming.MarkovClickStreamProcessing;
import ch.hsr.markovshield.kafkastream.streaming.StreamProcessing;
import ch.hsr.markovshield.kafkastream.streaming.StreamingApplication;
import io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.KStreamBuilder;
import org.apache.kafka.streams.processor.WallclockTimestampExtractor;
import org.apache.kafka.streams.state.HostInfo;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

public class MarkovShieldClickstreams {

    public static final String KAFKA_JOB_NAME = "MarkovShieldClickstreams";
    public static final String DEFAULT_BOOTSTRAP_SERVERS = "broker:9092";
    public static final String DEFAULT_SCHEMA_REGISTRY_URL = "http://schemaregistry:8081";
    private static final String ZOOKEEPER = "zookeeper:2181";
    private static final String DEFAULT_REST_ENDPOINT_HOSTNAME = "localhost";
    private static final int DEFAULT_REST_ENDPOINT_PORT = 7777;

    public static void main(final String[] args) throws Exception {
        Options options = getOptions();
        CommandLine commandLineArguments = getParsedArguments(args, options);
        if (commandLineArguments.hasOption("help")) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("gnu", options);
        } else {
            final HostInfo restEndpoint = parseHostInformation(commandLineArguments);
            final Properties streamsConfiguration = getStreamConfiguration(restEndpoint, commandLineArguments);
            setUpKafka(streamsConfiguration);
            StreamProcessing streamProcessing = new MarkovClickStreamProcessing();
            KStreamBuilder streamBuilder = streamProcessing.getStreamBuilder();
            StreamingApplication streamingApplication = new StreamingApplication(streamsConfiguration, streamBuilder);
            streamingApplication.startStreamingApp(restEndpoint);
        }

    }

    private static HostInfo parseHostInformation(CommandLine cmd) {
        final int restEndpointPort = getOption(cmd, "restport").map(s -> Integer.valueOf(s))
            .orElse(DEFAULT_REST_ENDPOINT_PORT);
        final String restEndpointHostname = getOption(cmd, "resthostname").orElse(DEFAULT_REST_ENDPOINT_HOSTNAME);
        return new HostInfo(restEndpointHostname, restEndpointPort);
    }

    private static CommandLine getParsedArguments(String[] args, Options options) throws ParseException {
        CommandLineParser parser = new DefaultParser();
        return parser.parse(options, args);
    }

    private static Options getOptions() {
        Options options = new Options();
        Option help = Option.builder("h").longOpt("help").desc("print this message").build();
        Option zookeeper = Option.builder()
            .longOpt("zookeeper")
            .hasArg()
            .numberOfArgs(1)
            .desc("address of the zookeeper")
            .build();
        Option schemaregistry = Option.builder()
            .longOpt("schemaregistry")
            .hasArg()
            .numberOfArgs(1)
            .desc("address of the schemaregistry")
            .build();
        Option bootstrap = Option.builder()
            .longOpt("bootstrap")
            .hasArg()
            .numberOfArgs(1)
            .desc("address of the kafka bootstrap")
            .build();
        Option resthostname = Option.builder()
            .longOpt("resthostname")
            .hasArg()
            .numberOfArgs(1)
            .desc("port of the REST endpoint")
            .build();
        Option restport = Option.builder()
            .longOpt("restport")
            .hasArg()
            .numberOfArgs(1)
            .desc("hostname of the REST endpoint")
            .build();
        options.addOption(help);
        options.addOption(zookeeper);
        options.addOption(schemaregistry);
        options.addOption(bootstrap);
        options.addOption(resthostname);
        options.addOption(restport);
        return options;
    }

    private static Optional<String> getOption(CommandLine commandLine, String option) {
        if (commandLine.hasOption(option)) {
            return Optional.ofNullable(commandLine.getOptionValue(option));
        } else {
            return Optional.empty();
        }
    }

    private static void setUpKafka(Properties streamsConfiguration) {
        Properties topicCreatorProperties = (Properties) streamsConfiguration.clone();
        topicCreatorProperties.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        topicCreatorProperties.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        KafkaConsumer consumer = new KafkaConsumer(topicCreatorProperties);
        Map map = consumer.listTopics();
        KafkaTopicCreator kafkaTopicCreator = new KafkaTopicCreator(ZOOKEEPER);
        createTopicIfNotPresent(map, kafkaTopicCreator, MarkovTopics.MARKOV_CLICK_STREAM_ANALYSIS_TOPIC);
        createTopicIfNotPresent(map, kafkaTopicCreator, MarkovTopics.MARKOV_CLICK_STREAM_TOPIC);
        createTopicIfNotPresent(map, kafkaTopicCreator, MarkovTopics.MARKOV_LOGIN_TOPIC);
        createTopicIfNotPresent(map, kafkaTopicCreator, MarkovTopics.MARKOV_USER_MODEL_TOPIC);
        createTopicIfNotPresent(map, kafkaTopicCreator, MarkovTopics.MARKOV_CLICK_TOPIC);
        createTopicIfNotPresent(map, kafkaTopicCreator, MarkovTopics.MARKOV_VALIDATED_CLICK_STREAMS);
        kafkaTopicCreator.closeConnection();
    }

    private static void createTopicIfNotPresent(Map map, KafkaTopicCreator kafkaTopicCreator, String topic) {
        if (!map.containsKey(topic)) {
            kafkaTopicCreator.createTopic(topic);
        }
    }

    private static Properties getStreamConfiguration(HostInfo restEndpoint, CommandLine cmd) {
        final String bootstrapServers = getOption(cmd, "bootstrap").orElse(DEFAULT_BOOTSTRAP_SERVERS);
        final String schemaRegistryUrl = getOption(cmd, "schemaregistry").orElse(DEFAULT_SCHEMA_REGISTRY_URL);
        final Serde<String> stringSerde = Serdes.String();
        final Properties streamsConfiguration = new Properties();
        streamsConfiguration.put(StreamsConfig.APPLICATION_ID_CONFIG, KAFKA_JOB_NAME);
        streamsConfiguration.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        streamsConfiguration.put(AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, schemaRegistryUrl);
        streamsConfiguration.put(StreamsConfig.KEY_SERDE_CLASS_CONFIG, stringSerde.getClass().getName());
        streamsConfiguration.put(StreamsConfig.VALUE_SERDE_CLASS_CONFIG, stringSerde.getClass().getName());
        streamsConfiguration.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        streamsConfiguration.put(StreamsConfig.COMMIT_INTERVAL_MS_CONFIG, 1000);
        streamsConfiguration.put(StreamsConfig.APPLICATION_SERVER_CONFIG,
            restEndpoint.host() + ":" + restEndpoint.port());
        streamsConfiguration.put(StreamsConfig.STATE_DIR_CONFIG, "kafka-store");
        streamsConfiguration.put(StreamsConfig.TIMESTAMP_EXTRACTOR_CLASS_CONFIG, WallclockTimestampExtractor.class);
        return streamsConfiguration;
    }
}


