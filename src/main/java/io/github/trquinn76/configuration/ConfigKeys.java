package io.github.trquinn76.configuration;

import java.util.Objects;

public record ConfigKeys(String key, 
        CmdLineArg commandLineArgument,
        String commandLineProperty, 
        String environmentVariable,
        String configFileProperty, 
        String defaultValue) {
    
    public ConfigKeys {
        Objects.requireNonNull(key);
        if (Objects.isNull(commandLineArgument) &&
                Utils.isNullOrEmpty(commandLineProperty) &&
                Utils.isNullOrEmpty(environmentVariable) &&
                Utils.isNullOrEmpty(configFileProperty) &&
                defaultValue == null) {
            throw new ConfigurationException("Config Keys require at least one of Command Line Argument, Command Line Property, Environment Variable, Configuration File Property or Default Value to be populated.");
        }
    }
    
    public static Builder newKeyBuilder(String key) {
        Objects.requireNonNull(key);
        if (key.isBlank()) {
            throw new ConfigurationException("Configuration key may not be empty or null.");
        }
        Builder retval = new Builder();
        return retval.key(key);
    }

    public static class Builder {
        private String key = null;
        private String commandLineArg = null;
        private String commandLineArgShort = null;
        private String commandLineProperty = null;
        private String envVariable = null;
        private String configFileProperty = null;
        private String defaultValue = null;
        
        public Builder key(String key) {
            this.key = key;
            return this;
        }
        
        // sets a Command Line Argument (should be descriptive name)
        public Builder cmdLineArgument(String cmdLineArgument) {
            this.commandLineArg = cmdLineArgument;
            return this;
        }
        
        // sets a short Command Line Argument (should be short and concise)
        public Builder cmdLineArgumentShort(String cmdLineArgumentShort) {
            this.commandLineArgShort = cmdLineArgumentShort;
            return this;
        }
        
        public Builder cmdLineProp(String cmdLineProp) {
            this.commandLineProperty = cmdLineProp;
            return this;
        }
        
        public Builder envVar(String envVar) {
            this.envVariable = envVar;
            return this;
        }
        
        public Builder configFileProp(String fileProp) {
            this.configFileProperty = fileProp;
            return this;
        }
        
        public Builder defaultValue(String defaultValue) {
            this.defaultValue = defaultValue;
            return this;
        }
        
        public ConfigKeys build() {
            CmdLineArg cmdLineArg = null;
            if (!Utils.isNullOrEmpty(commandLineArg)) {
                cmdLineArg = new CmdLineArg(commandLineArg, commandLineArgShort);
            }
            return new ConfigKeys(key, cmdLineArg, commandLineProperty, envVariable, configFileProperty, defaultValue);
        }
    }
}
