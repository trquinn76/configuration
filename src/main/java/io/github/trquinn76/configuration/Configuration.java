package io.github.trquinn76.configuration;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

public class Configuration {
    
    static {
        Configuration.addKey(ConfigKeys.newKey("configFile").cmdLineParam("configFile").cmdLineProp("configFile").envVar("CONFIG_FILE").build());
    }
    
    private static Set<ConfigKeys> keySet = new TreeSet<>();
    
    private static List<String> propertyFileList = new ArrayList<>();
    
    private static List<String> commandLineArgs = new ArrayList<>();
    
    // should be called in main() to preserve the command line arguments.
    public static void setCommandLineArgs(String[] args) {
        Objects.requireNonNull(args);
        commandLineArgs.addAll(Arrays.asList(args));
    }
    
    public static void addPropertyFile(String fileName) {
        propertyFileList.add(fileName);
    }
    
    public static void setPropertyFiles(String[] propertyFiles) {
        propertyFileList.clear();
        propertyFileList.addAll(List.of(propertyFiles));
    }
    
    public static void addKey(ConfigKeys key) {
        Objects.requireNonNull(key);
        keySet.forEach((existingKey) -> {
            if (key.key().equals(existingKey.key())) {
                throw new ConfigurationException("Attempting to add key " + key + " which duplicates the Key of " + existingKey);
            }
            if (key.commandLineParam().equals(existingKey.commandLineParam())) {
                throw new ConfigurationException("Attempting to add key " + key + " which duplicates the Command Line Param of " + existingKey);
            }
            if (key.commandLineProperty().equals(existingKey.commandLineProperty())) {
                throw new ConfigurationException("Attempting to add key " + key + " which duplicates the Command Line Property of " + existingKey);
            }
            if (key.environmentVariable().equals(existingKey.environmentVariable())) {
                throw new ConfigurationException("Attempting to add key " + key + " which duplicates the Environment Variable of " + existingKey);
            }
            if (key.configFileProperty().equals(existingKey.configFileProperty())) {
                throw new ConfigurationException("Attempting to add key " + key + " which duplicates the Configuration File Property of " + existingKey);
            }
        });
        keySet.add(key);
    }
    
    public static void addSharedKey(ConfigKeys key) {
        Objects.requireNonNull(key);
        keySet.add(key);
    }

    public String get(String key) {
        Object value = getValue(key);
        return getValueAsType(key, value, String.class);
    }
    
    public int getInt(String key) {
        Object value = getValue(key);
        Integer retval = getValueAsType(key, value, Integer.class);
        return retval.intValue();
    }
    
    public double getDouble(String key) {
        Object value = getValue(key);
        Double retval = getValueAsType(key, value, Double.class);
        return retval.doubleValue();
    }
    
    public boolean getBoolean(String key) {
        Object value = getValue(key);
        Boolean retval = getValueAsType(key, value, Boolean.class);
        return retval.booleanValue();
    }
    
    private Object getValue(String key) {
        ConfigKeys configKeys = keySet.stream().filter((configKey) -> configKey.key().equals(key)).findFirst().orElseThrow();
        String commandLineParam = getCommandLineArgument(configKeys.commandLineParam());
        if (commandLineParam != null && !commandLineParam.isBlank()) {
            return commandLineParam;
        }
        String commandLineProperty = System.getProperty(configKeys.commandLineProperty());
        if (commandLineProperty != null && !commandLineProperty.isBlank()) {
            return commandLineProperty;
        }
        String envValue = System.getenv(configKeys.environmentVariable());
        if (envValue != null && !envValue.isBlank()) {
            return envValue;
        }
        for (Properties properties : propertyFileList) {
            String value = properties.getProperty(configKeys.configFileProperty());
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        if (configKeys.defaultValue() != null) {
            return configKeys.defaultValue();
        }
        throw new ConfigurationException("No configuration value found for " + configKeys
                + "! At least a default value should have been found.");
    }
    
    private <T> T getValueAsType(String key, Object value, Class<T> clazz) {
        if (clazz.isInstance(value)) {
            return clazz.cast(value);
        }
        throw new ConfigurationException("Value for key " + key + " is not of type " + clazz.getCanonicalName());
    }
    
    private String getCommandLineArgument(String commandLineArgument) {
        if (commandLineArgument == null || commandLineArgument.isBlank()) {
            return null;
        }
        
        List<String> arguments;
        
        if (commandLineArgs.isEmpty()) {
            RuntimeMXBean runtimeMxBean = ManagementFactory.getRuntimeMXBean();
            arguments = runtimeMxBean.getInputArguments();
        }
        else {
            arguments = new ArrayList<>(commandLineArgs);
        }
        
        
        int idx = arguments.indexOf(commandLineArgument);
        if (idx > -1) {
            return arguments.get(idx + 1);
        }
        
        return null;
    }
}
