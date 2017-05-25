package ch.hsr.markovshield.flink;

import java.util.Properties;

public class KafkaConfigurationHelper {

    public static final String DEFAULT_BROKER = "broker:9092";
    public static final String DEFAULT_ZOOKEEPER = "zookeeper:2181";
    private final String broker;
    private final String zookeeper;
    private final String jobName;

    public KafkaConfigurationHelper(String broker, String zookeeper, String jobName) {
        this.broker = broker;
        this.zookeeper = zookeeper;
        this.jobName = jobName;
    }

    public KafkaConfigurationHelper(String jobName) {
        this.jobName = jobName;
        this.broker = DEFAULT_BROKER;
        this.zookeeper = DEFAULT_ZOOKEEPER;
    }

    public String getBroker() {
        return broker;
    }

    public String getZookeeper() {
        return zookeeper;
    }

    public String getJobName() {
        return jobName;
    }

    public Properties getKafkaProperties() {
        Properties properties = new Properties();
        properties.setProperty("bootstrap.servers", this.broker);
        properties.setProperty("zookeeper.connect", this.zookeeper);
        properties.setProperty("group.id", this.jobName);
        return properties;
    }
}
