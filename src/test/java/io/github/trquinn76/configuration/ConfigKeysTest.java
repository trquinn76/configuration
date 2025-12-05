package io.github.trquinn76.configuration;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class ConfigKeysTest {

    @Test
    void newConfigKeyTest() {
        ConfigKeys.Builder builder = ConfigKeys.newKeyBuilder("key");
        builder.cmdLineArgument("key").cmdLineArgumentShort("k");
        builder.cmdLineProp("propertyKey");
        builder.envVar("KEY");
        builder.configFileProp("propertyFileKey");
        builder.defaultValue("a key");
        
        ConfigKeys key = builder.build();
        
        assertEquals("key", key.key());
        assertEquals("key", key.commandLineArgument().argument());
        assertEquals("k", key.commandLineArgument().shortArgument());
        assertEquals("propertyKey", key.commandLineProperty());
        assertEquals("KEY", key.environmentVariable());
        assertEquals("propertyFileKey", key.configFileProperty());
        assertEquals("a key", key.defaultValue());
    }
    
    @Test
    void attemptToSetNullKey() {
        assertThrows(NullPointerException.class, () -> { new ConfigKeys(null, null, null, "KEY", null, null); });
    }
    
    @Test
    void attemptToSetNullKeyViaBuilder() {
        assertThrows(NullPointerException.class, () -> { ConfigKeys.newKeyBuilder(null); });
    }
    
    @Test
    void attemptToSetNullNonKeyValues() {
        assertThrows(ConfigurationException.class, () -> { new ConfigKeys("key", null, null, null, null, null); });
    }
    
    @Test
    void attemptToSetNullNonKeyValuesViaBuilder() {
        ConfigKeys.Builder builder = ConfigKeys.newKeyBuilder("key");
        assertThrows(ConfigurationException.class, () -> { builder.build(); });
    }
    
    // this represents a guess at the most common definition of Config Keys.
    @Test
    void createExpectedStandardKey() {
        ConfigKeys.Builder builder = ConfigKeys.newKeyBuilder("key");
        builder.envVar("KEY");
        builder.configFileProp("configFileProperty");
        ConfigKeys key = builder.build();
        
        assertEquals("key", key.key());
        assertEquals("KEY", key.environmentVariable());
        assertEquals("configFileProperty", key.configFileProperty());
        assertNull(key.commandLineArgument());
        assertNull(key.commandLineProperty());
        assertNull(key.defaultValue());
    }
    
    @Test
    void createCmdLineArgs() {
        ConfigKeys key = ConfigKeys.newKeyBuilder("key").cmdLineArgument("key").cmdLineArgumentShort("k").build();
        assertEquals("key", key.commandLineArgument().argument());
        assertEquals("k", key.commandLineArgument().shortArgument());
        
        key = ConfigKeys.newKeyBuilder("key").cmdLineArgument("key").build();
        assertEquals("key", key.commandLineArgument().argument());
        assertNull(key.commandLineArgument().shortArgument());
    }

}
