package ch.hsr.markovshield.flink;

import ch.hsr.markovshield.ml_models.builder.IQRFrequencyAnalysis;
import ch.hsr.markovshield.ml_models.builder.MarkovChainAnalysis;
import ch.hsr.markovshield.models.SimpleUserModelFactory;
import ch.hsr.markovshield.models.UserModelFactory;
import ch.hsr.markovshield.utils.OptionHelper;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

public class MarkovShieldModelUpdater {

    public static final int DEFAULT_REEVALUATION_INTERVAL_MINUTES = 1440;
    public static final int DEFAULT_SLIDING_TIME_MINUTES = 259200;
    public static final String FLINK_JOB_NAME = "MarkovShieldModelUpdater";
    public static final String KAFKA_JOB_NAME = "MarkovShieldModelUpdater";
    static final String LOOKBACKPERIOD_ARGUMENT_NAME = "lookbackperiod";
    static final String UPDATEINTERVAL_ARGUMENT_NAME = "updateinterval";
    static final String SESSION_TIMEOUT_ARGUMENT_NAME = "sessiontimeout";
    static final int DEFAULT_SESSION_TIME_MINUTES = 60;
    private static UserModelFactory userModelFactory;

    public static void main(final String[] args) throws Exception {
        userModelFactory = new SimpleUserModelFactory(new IQRFrequencyAnalysis(),
            new MarkovChainAnalysis());
        MarkovShieldModelUpdate markovShieldModelUpdate = new MarkovShieldModelUpdate(userModelFactory);

        Options options = getOptions();
        OptionHelper.displayHelpOrExecute(options, args,
            commandLineArguments -> {
                try {
                    markovShieldModelUpdate.executeModelUpdate(commandLineArguments);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
    }

    public static Options getOptions() {

        Options options = OptionHelper.getBasicKafkaOptions();
        Option updatetime = Option.builder()
            .longOpt(UPDATEINTERVAL_ARGUMENT_NAME)
            .hasArg()
            .numberOfArgs(1)
            .desc("time between the update of the usermodels in minutes, it's default is: " + DEFAULT_REEVALUATION_INTERVAL_MINUTES)
            .build();
        Option slidingtime = Option.builder()
            .longOpt(LOOKBACKPERIOD_ARGUMENT_NAME)
            .hasArg()
            .numberOfArgs(1)
            .desc("lookbackperiod of the usermodels in minutes, it's default is: " + DEFAULT_SLIDING_TIME_MINUTES)
            .build();
        Option sessiontime = Option.builder()
            .longOpt(SESSION_TIMEOUT_ARGUMENT_NAME)
            .hasArg()
            .numberOfArgs(1)
            .desc("session timeout in minutes, it's default is: " + DEFAULT_SESSION_TIME_MINUTES)
            .build();
        options.addOption(updatetime);
        options.addOption(slidingtime);
        options.addOption(sessiontime);
        return options;
    }
}

