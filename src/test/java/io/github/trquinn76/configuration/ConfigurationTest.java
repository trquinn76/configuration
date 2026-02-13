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
		
		Configuration.addKey(ConfigKeys.newKeyBuilder("alpha").cmdLineArgument("alpha").build());
		Configuration.addKey(ConfigKeys.newKeyBuilder("beta").cmdLineArgument("beta").build());
		Configuration.addKey(ConfigKeys.newKeyBuilder("gamma").cmdLineArgument("gamma").build());
		Configuration.addKey(ConfigKeys.newKeyBuilder("delta").cmdLineArgument("delta").build());
		
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
		
		Configuration.addKey(ConfigKeys.newKeyBuilder("alpha").cmdLineArgument("alpha").build());
		Configuration.addKey(ConfigKeys.newKeyBuilder("beta").cmdLineArgument("beta").build());
		Configuration.addKey(ConfigKeys.newKeyBuilder("gamma").cmdLineArgument("gamma").build());
		
		Configuration config = new Configuration();
		
		assertThrows(ConfigurationException.class, () -> config.getInt("alpha"));
		assertThrows(ConfigurationException.class, () -> config.getDouble("alpha"));
		assertFalse(config.getBoolean("gamma"));
	}
	
	@Test
	void noKeyTest() {
		Configuration.addKey(ConfigKeys.newKeyBuilder("alpha").cmdLineArgument("alpha").configFileProp("alpha").build());
		
		Configuration config = new Configuration();
		
		assertThrows(NoSuchElementException.class, () -> config.get("NonExistentKey"));
		assertThrows(ConfigurationException.class, () -> config.get("alpha"));
	}
}
