package ch.hsr.markovshield.utils;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import java.util.Optional;
import java.util.function.Consumer;

import static ch.hsr.markovshield.constants.KafkaConnectionDefaults.BOOTSTRAP_ARGUMENT_NAME;
import static ch.hsr.markovshield.constants.KafkaConnectionDefaults.DEFAULT_BOOTSTRAP_SERVERS;

public class OptionHelper {


    public static Optional<String> getOption(final CommandLine commandLine, final String option) {
        if (commandLine.hasOption(option)) {
            return Optional.ofNullable(commandLine.getOptionValue(option));
        } else {
            return Optional.empty();
        }
    }

    public static Options getBasicKafkaOptions() {
        Options options = new Options();
        Option help = Option.builder("h").longOpt("help").desc("print this message").build();

        Option bootstrap = Option.builder()
            .longOpt(BOOTSTRAP_ARGUMENT_NAME)
            .hasArg()
            .numberOfArgs(1)
            .desc("address of the kafka bootstrap, it's default is: " + DEFAULT_BOOTSTRAP_SERVERS)
            .build();
        options.addOption(help);
        options.addOption(bootstrap);
        return options;
    }

    public static void displayHelpOrExecute(final Options options, final String[] args, Consumer<CommandLine> execute) {
        CommandLine parsedArguments = null;
        try {
            parsedArguments = getParsedArguments(args, options);
        } catch (ParseException e) {
            System.out.println("Please enter valid options");
            return;
        }
        if (parsedArguments.hasOption("help")) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("gnu", options);
        } else {
            execute.accept(parsedArguments);
        }
    }

    private static CommandLine getParsedArguments(final String[] args, final Options options) throws ParseException {
        CommandLineParser parser = new DefaultParser();
        return parser.parse(options, args);
    }
}
