package ch.hsr.markovshield.flink;

import ch.hsr.markovshield.constants.KafkaConnectionDefaults;
import ch.hsr.markovshield.utils.OptionHelper;
import org.apache.commons.cli.CommandLine;
import java.util.Properties;

import static ch.hsr.markovshield.constants.KafkaConnectionDefaults.DEFAULT_ZOOKEEPER;
import static ch.hsr.markovshield.utils.OptionHelper.BOOTSTRAP_ARGUMENT_NAME;
import static ch.hsr.markovshield.utils.OptionHelper.ZOOKEEPER_ARGUMENT_NAME;

public class KafkaConfigurationHelper {

    private final String broker;
    private final String zookeeper;
    private final String jobName;

    public KafkaConfigurationHelper(String jobName, CommandLine commandLineArguments) {
        this.jobName = jobName;
        this.broker = OptionHelper.getOption(commandLineArguments, BOOTSTRAP_ARGUMENT_NAME).orElse(
            KafkaConnectionDefaults.DEFAULT_BOOTSTRAP_SERVERS);
        this.zookeeper = OptionHelper.getOption(commandLineArguments, ZOOKEEPER_ARGUMENT_NAME)
            .orElse(DEFAULT_ZOOKEEPER);
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
