package ch.hsr.markovshield.kafkastream.application;

import kafka.admin.AdminUtils;
import kafka.admin.RackAwareMode;
import kafka.utils.ZKStringSerializer$;
import kafka.utils.ZkUtils;
import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.ZkConnection;
import java.util.Properties;

public class KafkaTopicCreator {

    private static final int DEFAULT_SESSION_TIMEOUT_MS = 10 * 1000;
    private static final int DEFAULT_CONNECTION_TIMEOUT_MS = 8 * 1000;
    private static final int DEFAULT_PARTITIONS = 2;
    private static final int DEFAULT_REPLICATIONS = 1;
    private final ZkUtils zkUtils;
    private ZkClient zkClient;

    public KafkaTopicCreator(String zookeeper) {
        this(zookeeper, DEFAULT_SESSION_TIMEOUT_MS, DEFAULT_CONNECTION_TIMEOUT_MS);

    }

    public KafkaTopicCreator(String zookeeper, int sessionTimeoutMs, int connectionTimeoutMs) {
        zkClient = new ZkClient(
            zookeeper,
            sessionTimeoutMs,
            connectionTimeoutMs,
            ZKStringSerializer$.MODULE$);
        boolean isSecureKafkaCluster = false;
        zkUtils = new ZkUtils(zkClient, new ZkConnection(zookeeper), isSecureKafkaCluster);

    }

    public void createTopic(String topic) {
        createTopic(topic, DEFAULT_PARTITIONS, DEFAULT_REPLICATIONS, new Properties());

    }

    public void createTopic(String topic, int partitions, int replication, Properties topicConfig) {
        if (topic.isEmpty()) {
            return;
        }

        AdminUtils.createTopic(zkUtils, topic, partitions, replication, topicConfig, RackAwareMode.Enforced$.MODULE$);
    }

    public void createTopic(String topic, Properties topicConfig) {
        createTopic(topic, DEFAULT_PARTITIONS, DEFAULT_REPLICATIONS, topicConfig);
    }

    public void closeConnection() {
        zkClient.close();
    }

}