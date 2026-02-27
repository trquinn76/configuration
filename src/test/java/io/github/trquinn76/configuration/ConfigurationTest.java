package io.github.trquinn76.configuration;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class ConfigurationTest {
	
	@AfterEach
	void afterEach() {
		Configuration.clearConfig();
	}

	@Test
	void storeCmdLineArgsTest() {
		String[] args = {"alpha", "apple", "beta", "true", "gamma", "42", "delta", "1.11"};
		Configuration.storeCommandLineArgs(args);
		
		ConfigKey.newKeyBuilder("alpha").cmdLineArgument("alpha").buildAndAddKey();
		ConfigKey.newKeyBuilder("beta").cmdLineArgument("beta").buildAndAddKey();
		ConfigKey.newKeyBuilder("gamma").cmdLineArgument("gamma").buildAndAddKey();
		ConfigKey.newKeyBuilder("delta").cmdLineArgument("delta").buildAndAddKey();
		
		Configuration config = new Configuration();
		
		assertEquals("apple", config.get("alpha"));
		assertEquals(true, config.getBoolean("beta"));
		assertEquals(42, config.getInt("gamma"));
		assertEquals(1.11, config.getDouble("delta"));
	}

	@Test
	void storeCmdLineArgsInvalidTypesTest() {
		String args[] = { "alpha", "NotInt", "beta", "NotDouble", "gamma", "NotBoolean" };
		Configuration.storeCommandLineArgs(args);
		
		ConfigKey.newKeyBuilder("alpha").cmdLineArgument("alpha").buildAndAddKey();
		ConfigKey.newKeyBuilder("beta").cmdLineArgument("beta").buildAndAddKey();
		ConfigKey.newKeyBuilder("gamma").cmdLineArgument("gamma").buildAndAddKey();
		
		Configuration config = new Configuration();
		
		assertThrows(ConfigurationException.class, () -> config.getInt("alpha"));
		assertThrows(ConfigurationException.class, () -> config.getDouble("alpha"));
		assertFalse(config.getBoolean("gamma"));
	}
	
	@Test
	void noKeyTest() {
		ConfigKey.newKeyBuilder("alpha").cmdLineArgument("alpha").configFileProp("alpha").buildAndAddKey();
		
		Configuration config = new Configuration();
		
		assertThrows(NoSuchElementException.class, () -> config.get("NonExistentKey"));
		assertThrows(ConfigurationException.class, () -> config.get("alpha"));
	}
	
	@Test
	void fromPropertiesFileTest() {
		ConfigKey.newKeyBuilder("letters").configFileProp("alphabet").buildAndAddKey();
		ConfigKey.newKeyBuilder("fantasy").configFileProp("rumple").buildAndAddKey();
		
		Configuration.appendPropertyFile("test.properties");
		
		Configuration config = new Configuration();
		
		assertEquals("soup", config.get("letters"));
		assertEquals("stiltskin", config.get("fantasy"));
	}
	
	@Test
	void propertyFileFromConfigTest() {
		Configuration.appendPropertyFile("test.properties");
		
		System.setProperty("configFile", "alternative.properties");
		
		ConfigKey.newKeyBuilder("one").configFileProp("alphabet").buildAndAddKey();
		ConfigKey.newKeyBuilder("two").configFileProp("altkeyone").buildAndAddKey();
		
		Configuration config = new Configuration();
		
		assertEquals("soup", config.get("one"));
		assertEquals("alpha", config.get("two"));
	}
}
