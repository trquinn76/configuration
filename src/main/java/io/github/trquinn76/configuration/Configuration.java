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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Class for getting Configuration values. Intended for use in both Applications
 * and Libraries.
 * 
 * To add configuration keys create a static block, and use the
 * {@link ConfigKey.Builder} to create and add keys. eg:
 * {@code ConfigKey.newKeyBuilder("key").cmdLineArgument("-k").amdLineProp("key").envVar("KEY").buildAndAddKey()}
 * <p>
 * Configuration values are cached for accelerated retrieval. The
 * {@code clearCache()} function may be used to clear the cache and force a
 * reload of the configuration values. This function has been added for testing
 * and development purposes and should be avoided in production.
 * <p>
 * There is one existing configuration key, {@code configFile}. This may be used
 * to set a property file to search for configuration values at run time. This
 * property file will always be searched first, allowing otherwise hardcoded
 * configuration properties to be overridden.
 */
public class Configuration {

	private static Set<ConfigKey> keySet;
	private static List<String> propertyFileList;
	private static List<String> commandLineArgs;

	static {
		keySet = new HashSet<>();
		propertyFileList = new ArrayList<>();
		commandLineArgs = new ArrayList<>();
		ConfigKey.newKeyBuilder("configFile").cmdLineArgument("configFile").cmdLineProp("configFile")
				.envVar("CONFIG_FILE").buildAndAddKey();
	}
	
	private Map<String, String> cache = new HashMap<>();

	/**
	 * Used to store the Command Line Arguments. Should be called in the {@code main()} function if possible.
	 * 
	 * Java does not provide a reliable means of getting the Command Line Arguments outside of the {@code main()} function.
	 * This class will attempt to retrieve them via a call to {@code RuntimeMXBean.getInputArguments()} if the arguments
	 * have not been set via this function. However, that is not wholely reliable, and will vary between JVM implementations.
	 * 
	 * Where possible this function should be called in the {@code main()} function, like: {@code Configuration.storeCommandLineArgs(args)}
	 * 
	 * When using this in libraries, this is not possible, and avoiding Command Line Arguments is wisest. Command Line Properties are
	 * still available in this case.
	 * 
	 * @param args the Command Line Arguments passed into the application.
	 */
	public static void storeCommandLineArgs(String[] args) {
		Objects.requireNonNull(args);
		commandLineArgs.addAll(Arrays.asList(args));
	}

	/**
	 * Appends a new Property File name to the list of property files to search for Property values.
	 * 
	 * @param propertyFile the property file name to search for properties.
	 */
	public static void appendPropertyFile(String propertyFile) {
		propertyFileList.remove(propertyFile);
		propertyFileList.addLast(propertyFile);
	}

	/**
	 * Prepends a new Property File name to the list of property files to search for Property values.
	 * 
	 * @param propertyFile the property file name to search for properties.
	 */
	public static void prependPropertyFile(String propertyFile) {
		propertyFileList.remove(propertyFile);
		propertyFileList.addFirst(propertyFile);
	}

	/**
	 * Inserts a Property File name in the list of property files, at the given index.
	 * 
	 * @param index the index at which to insert the property file name.
	 * @param propertyFile the property file name to search for properties.
	 */
	public static void insertPropertyFile(int index, String propertyFile) {
		propertyFileList.add(index, propertyFile);
		removeDuplicateIndexesFromPropertyFileList(index, propertyFile);
	}

	/**
	 * Sets the list of Property File names to search for properties.
	 * 
	 * Will remove any existing Property File names.
	 * 
	 * Will only add distinct file names (ie: duplicates removed).
	 * 
	 * @param propertyFiles the list of Property File names to search for properties.
	 */
	public static void setPropertyFiles(Collection<String> propertyFiles) {
		propertyFileList.clear();
		propertyFileList.addAll(propertyFiles.stream().distinct().collect(Collectors.toList()));
	}

	/**
	 * An immutable list of the current Property File names.
	 * 
	 * @return the current, immutable, list of Property File names
	 */
	public static List<String> propertyFiles() {
		return Collections.unmodifiableList(propertyFileList);
	}

	/**
	 * Adds a new {@link ConfigKey} to the {@code Configuration}.
	 * 
	 * The new {@link ConfigKey} and all it's existing key values must be unique in {@code Configuration}.
	 * It is recommended to use reverse dns notation for each of the key values in the {@link ConfigKey} to
	 * avoid collisions.
	 * 
	 * @param key the unique {@link ConfigKey} to add.
	 */
	public static void addKey(ConfigKey key) {
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

	/**
	 * Adds a new shared {@link ConfigKey} to the {@code COnfiguration}.
	 * 
	 * Shared keys are for when multiple libraries depend on a common key. Then each library may add the key via this
	 * function. 
	 * 
	 * @param key the shared {@link ConfigKey} to add.
	 */
	public static void addSharedKey(ConfigKey key) {
		Objects.requireNonNull(key);
		keySet.add(key);
	}

	/**
	 * Get the configuration value for the given {@code key}.
	 * 
	 * @param key the configuration key for which to get the value.
	 * @return the configuration value for the given {@code key}.
	 */
	public String get(String key) {
		return getValue(key);
	}

	/**
	 * Gets the configuration value for the given {@code key} as an {@code int}.
	 * 
	 * @param key the configuration key for which to get the value.
	 * @return the configuration value for the given {@code key} as an {@code int}.
	 * @throws ConfigurationException if the value is not an integer.
	 */
	public int getInt(String key) {
		String value = getValue(key);
		return getValueAsInt(key, value);
	}

	/**
	 * Gets the configuration value for the given {@code key} as a {@code double}.
	 * 
	 * @param key the configuration key for which to get the value.
	 * @return the configuration value for the given {@code key} as a {@code double}.
	 * @throws ConfigurationException if the value is not a double.
	 */
	public double getDouble(String key) {
		String value = getValue(key);
		return getValueAsDouble(key, value);
	}

	/**
	 * Gets the configuration value for the given {@code key} as a {@code boolean}.
	 * 
	 * If the configuration value does not represent a boolean, then 'false' will be returned.
	 * 
	 * @param key the configuration key for which to get the value.
	 * @return the configuration value for the given {@code key} as a {@code boolean}.
	 */
	public boolean getBoolean(String key) {
		String value = getValue(key);
		return getValueAsBoolean(key, value);
	}
	
	/**
	 * Clears the configuration cache. Provided for testing and development. Should not be routinely used in production.
	 */
	public void clearCache() {
		this.cache.clear();
	}
	
	private String getValue(String key) {
		if (!cache.containsKey(key)) {
			cache.put(key, getConfiguredValue(key));
		}
		
		return cache.get(key);
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
	
	private String getConfiguredValue(String key) {
		ConfigKey configKeys = keySet.stream().filter((configKey) -> configKey.key().equals(key)).findFirst()
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
					if (!Utils.isNullOrBlank(value)) {
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

		ConfigKey configFileKey = keySet.stream().filter((configKey) -> configKey.key().equals("configFile"))
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
	
	private static void removeDuplicateIndexesFromPropertyFileList(int index, String propertyFile) {
		List<Integer> duplicateIndexes = new ArrayList<>();
		for (int i = 0; i < propertyFileList.size(); i++) {
			if (propertyFileList.get(i).equals(propertyFile) && i != index) {
				duplicateIndexes.add(i);
			}
		}
		// remove duplicate indexes in reverse order, so each removal does not impact index of other duplicates.
		duplicateIndexes.reversed().forEach(duplicateIndex -> propertyFileList.remove(duplicateIndex.intValue()));
	}
	
	/**
	 * Clears configuration data - intended for testing. Should not be used in production.
	 */
	static void clearConfig() {
		keySet.clear();
		propertyFileList.clear();
		commandLineArgs.clear();
		System.clearProperty("configFile");
		
		Configuration.addKey(ConfigKey.newKeyBuilder("configFile").cmdLineArgument("configFile")
				.cmdLineProp("configFile").envVar("CONFIG_FILE").build());
	}
}
