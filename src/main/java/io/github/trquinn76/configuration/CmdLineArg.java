package io.github.trquinn76.configuration;

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
