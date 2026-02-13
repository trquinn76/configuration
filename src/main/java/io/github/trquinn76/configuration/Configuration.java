package io.github.trquinn76.configuration;

import java.io.IOException;
import java.io.InputStream;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;

public class Configuration {

	private static Set<ConfigKeys> keySet;
	private static List<String> propertyFileList;
	private static List<String> commandLineArgs;

	static {
		keySet = new HashSet<>();
		propertyFileList = new ArrayList<>();
		commandLineArgs = new ArrayList<>();
		Configuration.addKey(ConfigKeys.newKeyBuilder("configFile").cmdLineArgument("configFile")
				.cmdLineProp("configFile").envVar("CONFIG_FILE").build());
	}

	// should be called in main() to preserve the command line arguments.
	public static void storeCommandLineArgs(String[] args) {
		Objects.requireNonNull(args);
		commandLineArgs.addAll(Arrays.asList(args));
	}

	public static void appendPropertyFile(String propertyFile) {
		propertyFileList.addLast(propertyFile);
	}

	public static void prependPropertyFile(String propertyFile) {
		propertyFileList.addFirst(propertyFile);
	}

	public static void insertPropertyFile(int index, String propertyFile) {
		propertyFileList.add(index, propertyFile);
	}

	public static void setPropertyFiles(Collection<String> propertyFiles) {
		propertyFileList.clear();
		propertyFileList.addAll(propertyFiles);
	}

	public static List<String> propertyFiles() {
		return Collections.unmodifiableList(propertyFileList);
	}

	public static void addKey(ConfigKeys key) {
		Objects.requireNonNull(key);
		keySet.forEach((existingKey) -> {
			if (key.key().equals(existingKey.key())) {
				throw new ConfigurationException(
						"Attempting to add key " + key + " which duplicates the Key of " + existingKey);
			}
			if (key.commandLineArgument() != null) {
				if (key.commandLineArgument().argument().equals(existingKey.commandLineArgument().argument())) {
					throw new ConfigurationException("Attempting to add key " + key
							+ " which duplicates the Command Line Argument of " + existingKey);
				}
				if (key.commandLineArgument().shortArgument() != null && key.commandLineArgument().shortArgument()
						.equals(existingKey.commandLineArgument().shortArgument())) {
					throw new ConfigurationException("Attempting to add key " + key
							+ " which duplicates the Short Command Line Argument of " + existingKey);
				}
			}
			if (key.commandLineProperty() != null
					&& key.commandLineProperty().equals(existingKey.commandLineProperty())) {
				throw new ConfigurationException("Attempting to add key " + key
						+ " which duplicates the Command Line Property of " + existingKey);
			}
			if (key.environmentVariable() != null
					&& key.environmentVariable().equals(existingKey.environmentVariable())) {
				throw new ConfigurationException("Attempting to add key " + key
						+ " which duplicates the Environment Variable of " + existingKey);
			}
			if (key.configFileProperty() != null && key.configFileProperty().equals(existingKey.configFileProperty())) {
				throw new ConfigurationException("Attempting to add key " + key
						+ " which duplicates the Configuration File Property of " + existingKey);
			}
		});
		keySet.add(key);
	}

	// for when multiple libraries depend on a common key, each library may add the
	// key via this function.
	public static void addSharedKey(ConfigKeys key) {
		Objects.requireNonNull(key);
		keySet.add(key);
	}

	public String get(String key) {
		return getValue(key);
	}

	public int getInt(String key) {
		String value = getValue(key);
		return getValueAsInt(key, value);
	}

	public double getDouble(String key) {
		String value = getValue(key);
		return getValueAsDouble(key, value);
	}

	public boolean getBoolean(String key) {
		String value = getValue(key);
		return getValueAsBoolean(key, value);
	}

	private String getValue(String key) {
		ConfigKeys configKeys = keySet.stream().filter((configKey) -> configKey.key().equals(key)).findFirst()
				.orElseThrow();
		String commandLineParam = getCommandLineArgument(configKeys.commandLineArgument());
		if (commandLineParam != null && !commandLineParam.isBlank()) {
			return commandLineParam;
		}
		if (configKeys.commandLineProperty() != null) {
			String commandLineProperty = System.getProperty(configKeys.commandLineProperty());
			if (commandLineProperty != null && !commandLineProperty.isBlank()) {
				return commandLineProperty;
			}
		}
		if (configKeys.environmentVariable() != null) {
			String envValue = System.getenv(configKeys.environmentVariable());
			if (envValue != null && !envValue.isBlank()) {
				return envValue;
			}
		}
		for (String propertyFile : generatePropertyFilesList()) {
			// assume a standard java Properties file.
			try {
				Properties properties = loadPropertiesFile(propertyFile);
				if (properties != null) {
					String value = properties.getProperty(configKeys.configFileProperty());
					if (!Utils.isNullOrEmpty(value)) {
						return value;
					}
				}
			} catch (IOException ioe) {
				throw new ConfigurationException("Failed to read Configuration file " + propertyFile, ioe);
			}
		}
		if (configKeys.defaultValue() != null) {
			return configKeys.defaultValue();
		}
		throw new ConfigurationException("No configuration value found for " + configKeys
				+ "! At least a default value should have been found.");
	}

	private int getValueAsInt(String key, String value) {
		try {
			return Integer.parseInt(value);
		} catch (NumberFormatException nfe) {
			throw new ConfigurationException("Value for key " + key + " is not of type Integer: " + nfe.getMessage());
		}
	}

	private double getValueAsDouble(String key, String value) {
		try {
			return Double.parseDouble(value);
		} catch (NumberFormatException nfe) {
			throw new ConfigurationException("Value for key " + key + " is not of type Double: " + nfe.getMessage());
		}
	}

	private boolean getValueAsBoolean(String key, String value) {
		return Boolean.parseBoolean(value);
	}

	private String getCommandLineArgument(CmdLineArg cmdLineArg) {
		if (cmdLineArg == null) {
			return null;
		}

		List<String> keys = new ArrayList<>();
		keys.add(cmdLineArg.argument());
		if (cmdLineArg.shortArgument() != null) {
			keys.add(cmdLineArg.shortArgument());
		}

		List<String> arguments;

		if (commandLineArgs.isEmpty()) {
			RuntimeMXBean runtimeMxBean = ManagementFactory.getRuntimeMXBean();
			arguments = runtimeMxBean.getInputArguments();
		} else {
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

	private List<String> generatePropertyFilesList() {
		List<String> propertyFiles = new ArrayList<>(propertyFileList);
		String userSetPropertyFile = null;

		ConfigKeys configFileKey = keySet.stream().filter((configKey) -> configKey.key().equals("configFile"))
				.findFirst().orElseThrow();
		String commandLineParam = getCommandLineArgument(configFileKey.commandLineArgument());
		if (commandLineParam != null && !commandLineParam.isBlank()) {
			userSetPropertyFile = commandLineParam;
		}
		String commandLineProperty = System.getProperty(configFileKey.commandLineProperty());
		if (userSetPropertyFile == null && commandLineProperty != null && !commandLineProperty.isBlank()) {
			userSetPropertyFile = commandLineProperty;
		}
		String envValue = System.getenv(configFileKey.environmentVariable());
		if (userSetPropertyFile == null && envValue != null && !envValue.isBlank()) {
			userSetPropertyFile = envValue;
		}

		if (userSetPropertyFile != null) {
			propertyFiles.addFirst(userSetPropertyFile);
		}

		return propertyFiles;
	}

	private static Properties loadPropertiesFile(String propertiesFileName) throws IOException {
		Enumeration<URL> resources = Thread.currentThread().getContextClassLoader().getResources(propertiesFileName);
		if (resources.hasMoreElements()) {
			try (InputStream in = Thread.currentThread().getContextClassLoader()
					.getResourceAsStream(propertiesFileName)) {
				Properties configProps = new Properties();
				configProps.load(in);

				return configProps;
			}
		}
		return null;
	}
	
	/**
	 * Clears configuration data - intended for testing. Should not be used in production.
	 */
	static void clearConfig() {
		keySet.clear();
		propertyFileList.clear();
		commandLineArgs.clear();
		
		Configuration.addKey(ConfigKeys.newKeyBuilder("configFile").cmdLineArgument("configFile")
				.cmdLineProp("configFile").envVar("CONFIG_FILE").build());
	}
}
