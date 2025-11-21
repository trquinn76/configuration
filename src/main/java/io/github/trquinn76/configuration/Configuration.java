package io.github.trquinn76.configuration;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

public class Configuration {
    
    static {
        Configuration.addKey(ConfigKeys.newKeyBuilder("configFile").cmdLineArgument("configFile").cmdLineProp("configFile").envVar("CONFIG_FILE").build());
    }
    
    private static Set<ConfigKeys> keySet = new TreeSet<>();
    
    private static List<File> propertyFileList = new ArrayList<>();
    
    private static List<String> commandLineArgs = new ArrayList<>();
    
    // should be called in main() to preserve the command line arguments.
    public static void storeCommandLineArgs(String[] args) {
        Objects.requireNonNull(args);
        commandLineArgs.addAll(Arrays.asList(args));
    }
    
    public static void appendPropertyFile(File propertyFile) {
        propertyFileList.addLast(propertyFile);
    }
    
    public static void prependPropertyFile(File propertyFile) {
        propertyFileList.addFirst(propertyFile);
    }
    
    public static void insertPropertyFile(int index, File propertyFile) {
        propertyFileList.add(index, propertyFile);
    }
    
    public static void setPropertyFiles(Collection<File> propertyFiles) {
        propertyFileList.clear();
        propertyFileList.addAll(propertyFiles);
    }
    
    public static List<File> propertyFiles() {
        return Collections.unmodifiableList(propertyFileList);
    }
    
    public static void addKey(ConfigKeys key) {
        Objects.requireNonNull(key);
        keySet.forEach((existingKey) -> {
            if (key.key().equals(existingKey.key())) {
                throw new ConfigurationException("Attempting to add key " + key + " which duplicates the Key of " + existingKey);
            }
            if (key.commandLineArgument().argument().equals(existingKey.commandLineArgument().argument())) {
                throw new ConfigurationException("Attempting to add key " + key + " which duplicates the Command Line Argument of " + existingKey);
            }
            if (key.commandLineArgument().shortArgument().equals(existingKey.commandLineArgument().shortArgument())) {
                throw new ConfigurationException("Attempting to add key " + key + " which duplicates the Short Command Line Argument of " + existingKey);
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
    
    // for when multiple libraries depend on a common key, each library may add the key via this function.
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
        String commandLineParam = getCommandLineArgument(configKeys.commandLineArgument());
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
        for (File propertyFile : generatePropertyFilesList()) {
            if (propertyFile.exists()) {
                // assume a standard java Properties file.
                try {
                    Properties properties = new Properties();
                    properties.load(new FileInputStream(propertyFile));
                    String value = properties.getProperty(configKeys.configFileProperty());
                    if (value != null && !value.isBlank()) {
                        return value;
                    }
                }
                catch (IOException ioe) {
                    throw new ConfigurationException("Failed to read Configuration file " + propertyFile.getName(), ioe);
                }
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
    
    private String getCommandLineArgument(CmdLineArg cmdLineArg) {
        if (cmdLineArg == null) {
            return null;
        }
        
        List<String> keys = List.of( cmdLineArg.argument(), cmdLineArg.shortArgument() );
        
        List<String> arguments;
        
        if (commandLineArgs.isEmpty()) {
            RuntimeMXBean runtimeMxBean = ManagementFactory.getRuntimeMXBean();
            arguments = runtimeMxBean.getInputArguments();
        }
        else {
            arguments = new ArrayList<>(commandLineArgs);
        }
        
        for (String key : keys) {
            int idx = arguments.indexOf(key);
            if (idx > -1) {
                return arguments.get(idx + 1);
            }
        }
        
        return null;
    }
    
    private List<File> generatePropertyFilesList() {
        List<File> propertyFiles = new ArrayList<>(propertyFileList);
        File userSetPropertyFile = null;
        
        ConfigKeys configFileKey = keySet.stream().filter((configKey) -> configKey.key().equals("configFile")).findFirst().orElseThrow();
        String commandLineParam = getCommandLineArgument(configFileKey.commandLineArgument());
        if (commandLineParam != null && !commandLineParam.isBlank()) {
            userSetPropertyFile = new File(commandLineParam);
        }
        String commandLineProperty = System.getProperty(configFileKey.commandLineProperty());
        if (commandLineProperty != null && !commandLineProperty.isBlank()) {
            userSetPropertyFile = new File(commandLineProperty);
        }
        String envValue = System.getenv(configFileKey.environmentVariable());
        if (envValue != null && !envValue.isBlank()) {
            userSetPropertyFile = new File(envValue);
        }
        
        if (userSetPropertyFile != null) {
            propertyFiles.addFirst(userSetPropertyFile);
        }
        
        return propertyFiles;
    }
}
