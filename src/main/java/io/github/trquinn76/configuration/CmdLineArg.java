package io.github.trquinn76.configuration;

/**
 * Defines a Command Line Argument.
 * 
 * If you wish your command line arguments to use the '-' character, such as
 * '-v', then it needs to be explicitly included in the {@code argument} and
 * optional {@code shortArgument}.
 */
public record CmdLineArg(String argument, 
        String shortArgument) {

    public CmdLineArg {
        if (Utils.isNullOrEmpty(argument)) {
            throw new ConfigurationException("Require a command line argument.");
        }
    }
    
    public CmdLineArg(String argument) {
        this(argument, null);
    }
}
