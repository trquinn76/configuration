package io.github.trquinn76.configuration;

import java.util.Objects;

public record ConfigKeys(String key, String commandLineParamShort, String commandLineParam, String commandLineProperty, String environmentVariable,
        String configFileProperty, Object defaultValue) {
    
    public ConfigKeys {
        Objects.requireNonNull(key);
        if (Utils.isNullOrEmpty(commandLineParamShort) &&
                Utils.isNullOrEmpty(commandLineParam) &&
                Utils.isNullOrEmpty(commandLineProperty) &&
                Utils.isNullOrEmpty(environmentVariable) &&
                Utils.isNullOrEmpty(configFileProperty) &&
                defaultValue == null) {
            throw new ConfigurationException("Config Keys require at least one of Command Line Parameter, Command Line Property, Environment Variable, Configuration File Property or Default Value to be populated.");
        }
    }
    
    public static Builder newKey(String key) {
        Objects.requireNonNull(key);
        if (key.isBlank()) {
            throw new ConfigurationException("Configuration key may not be empty or null.");
        }
        Builder retval = new Builder();
        return retval.key(key);
    }

    public static class Builder {
        String key = null;
        String commandLineParamShort = null;
        String commandLineParam = null;
        String commandLineProperty = null;
        String envVariable = null;
        String configFileProperty = null;
        Object defaultValue = null;
        
        public Builder key(String key) {
            this.key = key;
            return this;
        }
        
        public Builder cmdLineParamShort(String cmdLineParamShort) {
            this.commandLineParamShort = cmdLineParamShort;
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
            return new ConfigKeys(key, commandLineParamShort, commandLineParam, commandLineProperty, envVariable, configFileProperty, defaultValue);
        }
    }
}
