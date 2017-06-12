package ch.hsr.markovshield.flink;

import ch.hsr.markovshield.constants.KafkaConnectionDefaults;
import ch.hsr.markovshield.utils.OptionHelper;
import org.apache.commons.cli.CommandLine;
import java.util.Properties;

import static ch.hsr.markovshield.constants.KafkaConnectionDefaults.BOOTSTRAP_ARGUMENT_NAME;

public class KafkaConfigurationHelper {

    private final String broker;

    private final String jobName;

    public KafkaConfigurationHelper(String jobName, CommandLine commandLineArguments) {
        this.jobName = jobName;
        this.broker = OptionHelper.getOption(commandLineArguments, BOOTSTRAP_ARGUMENT_NAME).orElse(
            KafkaConnectionDefaults.DEFAULT_BOOTSTRAP_SERVERS);
    }

    public String getBroker() {
        return broker;
    }


    public String getJobName() {
        return jobName;
    }

    public Properties getKafkaProperties() {
        Properties properties = new Properties();
        properties.setProperty("bootstrap.servers", this.broker);
        properties.setProperty("group.id", this.jobName);
        return properties;
    }
}
