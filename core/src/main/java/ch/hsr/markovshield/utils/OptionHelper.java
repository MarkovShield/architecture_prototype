package ch.hsr.markovshield.utils;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import java.util.Optional;
import java.util.function.Consumer;

public class OptionHelper {

    public static Optional<String> getOption(final CommandLine commandLine, final String option) {
        if (commandLine.hasOption(option)) {
            return Optional.ofNullable(commandLine.getOptionValue(option));
        } else {
            return Optional.empty();
        }
    }

    private static CommandLine getParsedArguments(final String[] args, final Options options) throws ParseException {
        CommandLineParser parser = new DefaultParser();
        return parser.parse(options, args);
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
}
