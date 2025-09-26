package io.github.trquinn76.configuration;

import java.util.Objects;

public record ConfigKeys(String key, String commandLineParam, String commandLineProperty, String envVariable,
        String configFileProperty, Object defaultValue) {
    
    public ConfigKeys {
        Objects.requireNonNull(key);
        if (Utils.isNullOrEmpty(commandLineParam) &&
                Utils.isNullOrEmpty(commandLineProperty) &&
                Utils.isNullOrEmpty(envVariable) &&
                Utils.isNullOrEmpty(configFileProperty) &&
                defaultValue == null) {
            throw new ConfigurationException("Config Keys require at least one of Command Line Paramater, Command Line Property, Environment Variable, Configuration File Property or Default Value to be populated.");
        }
    }
    
    public static Builder init(String key) {
        Objects.requireNonNull(key);
        if (key.isBlank()) {
            throw new ConfigurationException("Configuration key may not be empty or null.");
        }
        Builder retval = new Builder();
        return retval.key(key);
    }

    public static class Builder {
        String key = null;
        String commandLineParam = null;
        String commandLineProperty = null;
        String envVariable = null;
        String configFileProperty = null;
        Object defaultValue = null;
        
        public Builder key(String key) {
            this.key = key;
            return this;
        }
        
        public Builder cmdLineParam(String cmdLineParam) {
            this.commandLineParam = cmdLineParam;
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
        
        public Builder defaultValue(Object defaultValue) {
            this.defaultValue = defaultValue;
            return this;
        }
        
        public ConfigKeys build() {
            return new ConfigKeys(key, commandLineParam, commandLineProperty, envVariable, configFileProperty, defaultValue);
        }
    }
}
