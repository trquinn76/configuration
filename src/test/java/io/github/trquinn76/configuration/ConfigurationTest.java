package io.github.trquinn76.configuration;

import static org.junit.jupiter.api.Assertions.*;

import java.util.NoSuchElementException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import uk.org.webcompere.systemstubs.environment.EnvironmentVariables;
import uk.org.webcompere.systemstubs.jupiter.SystemStub;
import uk.org.webcompere.systemstubs.jupiter.SystemStubsExtension;

@ExtendWith(SystemStubsExtension.class)
class ConfigurationTest {
	
	@SystemStub
    private EnvironmentVariables environment = new EnvironmentVariables();
	
	@AfterEach
	void afterEach() {
		Configuration.clearConfig();
	}
	
	// Key Tests
	@Test
	void noKeyTest() {
		ConfigKey.newKeyBuilder("alpha").cmdLineArgument("alpha").configFileProp("alpha").buildAndAddKey();
		
		Configuration config = new Configuration();
		
		assertThrows(NoSuchElementException.class, () -> config.get("NonExistentKey"));
		assertThrows(ConfigurationException.class, () -> config.get("alpha"));
	}
	
	@Test
	void noPropertiesTest() {
		assertThrows(ConfigurationException.class, () -> ConfigKey.newKeyBuilder("alpha").build());
	}
	
	// Command Line Args Tests
	@Test
	void storeCmdLineArgsTest() {
		String[] args = {"-alpha", "apple", "-beta", "true", "-gamma", "42", "-delta", "1.11"};
		Configuration.storeCommandLineArgs(args);
		
		ConfigKey.newKeyBuilder("alpha").cmdLineArgument("-alpha").buildAndAddKey();
		ConfigKey.newKeyBuilder("beta").cmdLineArgument("-beta").buildAndAddKey();
		ConfigKey.newKeyBuilder("gamma").cmdLineArgument("-gamma").buildAndAddKey();
		ConfigKey.newKeyBuilder("delta").cmdLineArgument("-delta").buildAndAddKey();
		
		Configuration config = new Configuration();
		
		assertEquals("apple", config.get("alpha"));
		assertEquals(true, config.getBoolean("beta"));
		assertEquals(42, config.getInt("gamma"));
		assertEquals(1.11, config.getDouble("delta"));
	}

	@Test
	void storeCmdLineArgsInvalidTypesTest() {
		String args[] = { "-alpha", "NotInt", "-beta", "NotDouble", "-gamma", "NotBoolean" };
		Configuration.storeCommandLineArgs(args);
		
		ConfigKey.newKeyBuilder("alpha").cmdLineArgument("-alpha").buildAndAddKey();
		ConfigKey.newKeyBuilder("beta").cmdLineArgument("-beta").buildAndAddKey();
		ConfigKey.newKeyBuilder("gamma").cmdLineArgument("-gamma").buildAndAddKey();
		
		Configuration config = new Configuration();
		
		assertThrows(ConfigurationException.class, () -> config.getInt("alpha"));
		assertThrows(ConfigurationException.class, () -> config.getDouble("beta"));
		assertFalse(config.getBoolean("gamma"));
	}
	
	@Test
	void ignoreNonConfigCmdLineArgsTest() {
		String args[] = { "-alpha", "apple", "NotConfig", "-beta", "bananna" };
		Configuration.storeCommandLineArgs(args);
		
		ConfigKey.newKeyBuilder("alpha").cmdLineArgument("-alpha").buildAndAddKey();
		ConfigKey.newKeyBuilder("beta").cmdLineArgument("-beta").buildAndAddKey();
		
		Configuration config = new Configuration();
		
		assertEquals("apple", config.get("alpha"));
		assertEquals("bananna", config.get("beta"));
	}
	
	@Test
	void longAndShortArgsTest() {
		String args[] = { "-alpha", "apple", "-b", "bananna", "-gamma", "grape", "-d", "Drink" };
		Configuration.storeCommandLineArgs(args);
		
		ConfigKey.newKeyBuilder("alpha").cmdLineArgument("-alpha").cmdLineArgumentShort("-a").buildAndAddKey();
		ConfigKey.newKeyBuilder("beta").cmdLineArgument("-beta").cmdLineArgumentShort("-b").buildAndAddKey();
		ConfigKey.newKeyBuilder("gamma").cmdLineArgument("-gamma").cmdLineArgumentShort("-g").buildAndAddKey();
		ConfigKey.newKeyBuilder("delta").cmdLineArgument("-delta").cmdLineArgumentShort("-d").buildAndAddKey();
		
		Configuration config = new Configuration();
		
		assertEquals("apple", config.get("alpha"));
		assertEquals("bananna", config.get("beta"));
		assertEquals("grape", config.get("gamma"));
		assertEquals("Drink", config.get("delta"));
	}
	
	// Command Line Property Tests
	@Test
	void cmdLinePropertiesTest() {
		System.setProperty("alpha", "apple");
		System.setProperty("beta", "bananna");
		
		ConfigKey.newKeyBuilder("alpha").cmdLineProp("alpha").buildAndAddKey();
		ConfigKey.newKeyBuilder("beta").cmdLineProp("beta").buildAndAddKey();
		
		Configuration config = new Configuration();
		
		assertEquals("apple", config.get("alpha"));
		assertEquals("bananna", config.get("beta"));
		
		// clear test properties
		System.clearProperty("alpha");
		System.clearProperty("beta");
	}
	
	// Environment Variable Tests
	@Test
	void environmentVariableTest() {
		environment.set("ALPHA", "apple");
		environment.set("BETA", "bananna");
		
		ConfigKey.newKeyBuilder("alpha").envVar("ALPHA").buildAndAddKey();
		ConfigKey.newKeyBuilder("beta").envVar("BETA").buildAndAddKey();
		
		Configuration config = new Configuration();
		
		assertEquals("apple", config.get("alpha"));
		assertEquals("bananna", config.get("beta"));
	}
	
	// Property File Tests
	//   - property file from config tests
	//   - file order tests
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
	
	@Test
	void testAltOrderPropertyFilesTest() {
		Configuration.appendPropertyFile("test.properties");
		Configuration.appendPropertyFile("alternative.properties");
		
		ConfigKey.newKeyBuilder("gamma").configFileProp("gamma").buildAndAddKey();
		
		Configuration config = new Configuration();
		
		assertEquals("gamma test", config.get("gamma"));
	}
	
	@Test
	void altTestOrderPropertyFilesTest() {
		Configuration.appendPropertyFile("alternative.properties");
		Configuration.appendPropertyFile("test.properties");
		
		ConfigKey.newKeyBuilder("gamma").configFileProp("gamma").buildAndAddKey();
		
		Configuration config = new Configuration();
		
		assertEquals("gamma alternative", config.get("gamma"));
	}
	
	// Default Tests
	@Test
	void defaultTest() {
		ConfigKey.newKeyBuilder("alpha").defaultValue("apple").buildAndAddKey();
		ConfigKey.newKeyBuilder("beta").defaultValue("bananna").buildAndAddKey();
		
		Configuration config = new Configuration();
		
		assertEquals("apple", config.get("alpha"));
		assertEquals("bananna", config.get("beta"));
	}
	
	// Precedence Order Tests
	@Test
	void precedenceOrderTest() {
		ConfigKey.newKeyBuilder("key").cmdLineArgument("-key").cmdLineProp("key").envVar("KEY").configFileProp("prop.key").defaultValue("default value").buildAndAddKey();
		
		Configuration config = new Configuration();
		
		assertEquals("default value", config.get("key"));
		
		Configuration.appendPropertyFile("precedenceOrder.properties");
		
		assertEquals("property file value", config.get("key"));
		
		environment.set("KEY", "environment variable value");
		
		assertEquals("environment variable value", config.get("key"));
		
		System.setProperty("key", "cmd line property value");
		
		assertEquals("cmd line property value", config.get("key"));
		
		String args[] = { "-key", "cmd line argument value" };
		Configuration.storeCommandLineArgs(args);
		
		assertEquals("cmd line argument value", config.get("key"));
	}
	
	
	
	
	
}
