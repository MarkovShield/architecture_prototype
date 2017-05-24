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

        Properties topicConfig = new Properties(); // add per-topic configurations settings here

        // Note: You must initialize the ZkClient with ZKStringSerializer.  If you don't, then
        // createTopic() will only seem to work (it will return without error).  The topic will exist in
        // only ZooKeeper and will be returned when listing topics, but Kafka itself does not create the
        // topic.

        // Security for Kafka was added in Kafka 0.9.0.0

        AdminUtils.createTopic(zkUtils, topic, partitions, replication, topicConfig, RackAwareMode.Enforced$.MODULE$);
    }

    public void closeConnection() {
        zkClient.close();
    }

}