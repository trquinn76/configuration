package io.github.trquinn76.configuration;

import java.util.Objects;

/**
 * Represents a configuration key, with values for each configuration source.
 * 
 * <p>
 * A Configuration {@code key}, with at least one or more of the following keys:
 * <ul>
 * <li>Command Line Argument</li>
 * <li>Command Line Property</li>
 * <li>Environment Variable</li>
 * <li>Property File Property</li>
 * <li>Default Value</li>
 * </ul>
 * 
 * <p>
 * When using {@link Configuration} {@code getValue()} functions to get a
 * configuration value, the {@code key} value is the one to use. Then the
 * configuration value will be retrieved, using the various key values.
 * 
 * @param key                 the key of the Configuration value. Used to
 *                            retrieve the value from the instance of
 *                            {@link Configuration}.
 * @param commandLineArgument optional command line argument, which may populate
 *                            the configuration value.
 * @param commandLineProperty optional command line property, which may populate
 *                            the configuration value.
 * @param environmentVariable optional environment variable, which may populate
 *                            the configuration value.
 * @param configFileProperty  optional property file Property, which may have a
 *                            value which populates the configuration value.
 * @param noValueAllowed      indicates that the configuration value may be
 *                            unset. Is false by default - it is expected that
 *                            in normal circumstances configuration values will
 *                            always have a value.
 * @param defaultValue        an optional default value for the configuration
 *                            value.
 */
public record ConfigKey(String key, CmdLineArg commandLineArgument, String commandLineProperty,
		String environmentVariable, String configFileProperty, boolean noValueAllowed, String defaultValue) {

	/**
	 * A {@code ConfigKey} must have a {@code key}, and at least one other key or
	 * value.
	 * 
	 * @param key                 the {@code String} used to reference this
	 *                            configuration value throughout the code.
	 * @param commandLineArgument a {@link CmdLineArg} which represents the command
	 *                            line argument (and a short form) which may provide
	 *                            this configuration value.
	 * @param commandLineProperty a {@code String} which represents the command line
	 *                            property which may provide this configuration
	 *                            value.
	 * @param environmentVariable a {@code String} which represents the environment
	 *                            variable which may provide this configuration
	 *                            value.
	 * @param configFileProperty  a {@code String} which represents a property key,
	 *                            found in a property file, which may provide this
	 *                            configuration value.
	 * @param noValueAllowed      a {@code Boolean} which indicates that no value is
	 *                            permitted for this key. Is false by default.
	 *                            Configuration keys should nearly always have an
	 *                            associated value. Designs where configuration
	 *                            values may be absent should be reconsidered. This
	 *                            supports the rare case where a configuration value
	 *                            is permitted to be absent.
	 * @param defaultValue        a {@code String} which represents an optional
	 *                            default value for the key.
	 */
	public ConfigKey {
		Objects.requireNonNull(key);
		if (Objects.isNull(commandLineArgument) && Utils.isNullOrBlank(commandLineProperty)
				&& Utils.isNullOrBlank(environmentVariable) && Utils.isNullOrBlank(configFileProperty)
				&& !noValueAllowed && defaultValue == null) {
			throw new ConfigurationException(
					"Config Keys require at least one of Command Line Argument, Command Line Property, Environment Variable, Configuration File Property or Default Value to be populated.");
		}
	}

	/**
	 * Create a {@link Builder} for {@link ConfigKey}, with the given {@code key} as
	 * the configuration key.
	 * 
	 * @param key the {@code key} for the configuration value, which may be used to
	 *            retrieve the configuration value from the instance of
	 *            {@link Configuration}.
	 * @return a new {@link Builder} for an instance of {@link ConfigKey}.
	 */
	public static Builder newKeyBuilder(String key) {
		if (Utils.isNullOrBlank(key)) {
			throw new ConfigurationException("Configuration key may not be null or empty.");
		}
		Builder retval = new Builder();
		return retval.key(key);
	}

	/**
	 * A Builder which may be used to create instances of {@link ConfigKey} as well
	 * as add them to {@link Configuration}.
	 */
	public static class Builder {
		private String key = null;
		private String commandLineArg = null;
		private String commandLineArgShort = null;
		private String commandLineProperty = null;
		private String envVariable = null;
		private String configFileProperty = null;
		private boolean noValueAllowed = false;
		private String defaultValue = null;

		/**
		 * Sets the key value.
		 * 
		 * @param key the key value to set.
		 * @return this for function chaining.
		 */
		public Builder key(String key) {
			this.key = key;
			return this;
		}

		/**
		 * Sets a Command Line Argument (should be descriptive)
		 * 
		 * Should explicitly include the '-' character if that is desired in the command
		 * line.
		 * 
		 * @param cmdLineArgument the command line argument to set.
		 * @return this for function chaining.
		 */
		public Builder cmdLineArgument(String cmdLineArgument) {
			this.commandLineArg = cmdLineArgument;
			return this;
		}

		/**
		 * Sets a short Command Line Argument (should be short and concise)
		 * 
		 * Should explicitly include the '-' character if that is desired in the command
		 * line.
		 * 
		 * @param cmdLineArgumentShort the short command line argument to set.
		 * @return this for function chaining.
		 */
		public Builder cmdLineArgumentShort(String cmdLineArgumentShort) {
			this.commandLineArgShort = cmdLineArgumentShort;
			return this;
		}

		/**
		 * Sets a Command Line Property
		 * 
		 * @param cmdLineProp the command line property to set.
		 * @return this for function chaining.
		 */
		public Builder cmdLineProp(String cmdLineProp) {
			this.commandLineProperty = cmdLineProp;
			return this;
		}

		/**
		 * Sets an Environment Variable
		 * 
		 * @param envVar the environment variable to set.
		 * @return this for function chaining.
		 */
		public Builder envVar(String envVar) {
			this.envVariable = envVar;
			return this;
		}

		/**
		 * Sets a Property File key.
		 * 
		 * @param fileProp the property file key to set.
		 * @return this for function chaining.
		 */
		public Builder configFileProp(String fileProp) {
			this.configFileProperty = fileProp;
			return this;
		}

		/**
		 * Sets a flag which indicates if no value is allowed for this key.
		 * 
		 * If true, then the default value may be null, and no error will be raised if
		 * no value is found for this config key.
		 * 
		 * @param noValueAllowed indicates if this config key may have no value.
		 * @return this for function chaining.
		 */
		public Builder noValueAllowed(boolean noValueAllowed) {
			this.noValueAllowed = noValueAllowed;
			return this;
		}

		/**
		 * Sets a default value to return if there is no configured value.
		 * 
		 * @param defaultValue the default value to return if there is no other
		 *                     configured value.
		 * @return this for function chaining.
		 */
		public Builder defaultValue(String defaultValue) {
			this.defaultValue = defaultValue;
			return this;
		}

		/**
		 * Creates and returns an instacnce of {@link ConfigKey} from the existing
		 * values.
		 * 
		 * @return a new {@link ConfigKey} created from the values in this builder.
		 */
		public ConfigKey build() {
			CmdLineArg cmdLineArg = null;
			if (!Utils.isNullOrBlank(commandLineArg)) {
				cmdLineArg = new CmdLineArg(commandLineArg, commandLineArgShort);
			}
			return new ConfigKey(key, cmdLineArg, commandLineProperty, envVariable, configFileProperty, noValueAllowed,
					defaultValue);
		}

		/**
		 * Creates and adds new {@link ConfigKey} to the {@link Configuration} via it's
		 * {@code addKey()} function.
		 */
		public void buildAndAddKey() {
			Configuration.addKey(build());
		}

		/**
		 * Creates and adds new {@link ConfigKey} to the {@link Configuration} via it's
		 * {@code addSharedKey()} function.
		 */
		public void buildAndAddSharedKey() {
			Configuration.addSharedKey(build());
		}
	}
}
