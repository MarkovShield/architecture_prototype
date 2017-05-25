package ch.hsr.markovshield.kafkastream.streaming;

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
        createTopic(topic, DEFAULT_PARTITIONS, DEFAULT_REPLICATIONS);

    }

    public void createTopic(String topic, int partitions, int replication) {
        if (topic.isEmpty()) {
            return;
        }

        Properties topicConfig = new Properties();
        AdminUtils.createTopic(zkUtils, topic, partitions, replication, topicConfig, RackAwareMode.Enforced$.MODULE$);
    }

    public void closeConnection() {
        zkClient.close();
    }

}