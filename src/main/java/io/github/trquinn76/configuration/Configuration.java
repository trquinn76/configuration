package io.github.trquinn76.configuration;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

public class Configuration {
    
    static {
        Configuration.addKey(ConfigKeys.init("configFile").cmdLineParam("configFile").cmdLineProp("configFile").envVar("CONFIG_FILE").build());
    }
    
    private static Set<ConfigKeys> keys = new TreeSet<>();
    
    private static List<String> propertyFileList = new ArrayList<>();
    
    public static void addPropertyFile(String fileName) {
        propertyFileList.add(fileName);
    }
    
    public static void setPropertyFiles(String[] propertyFiles) {
        propertyFileList.clear();
        propertyFileList.addAll(List.of(propertyFiles));
    }
    
    public static void addKey(ConfigKeys key) {
        Objects.requireNonNull(key);
        keys.add(key);
    }

    private Map<String, Object> configurationValuesMap = new TreeMap<>();
    
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
        Object value = configurationValuesMap.get(key);
        if (value == null) {
            throw new ConfigurationException("No configuration value for key: " + key);
        }
        return value;
    }
    
    private <T> T getValueAsType(String key, Object value, Class<T> clazz) {
        if (clazz.isInstance(value)) {
            return clazz.cast(value);
        }
        throw new ConfigurationException("Value for key " + key + " is not of type " + clazz.getCanonicalName());
    }
}
