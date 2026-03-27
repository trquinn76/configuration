package io.github.trquinn76.configuration;

import java.util.Objects;

/**
 * Represents a configuration key, with values for each configuration source.
 * 
 * <ul>
 * <li>Command Line Arguments</li>
 * <li>Command Line Properties</li>
 * <li>Environment Variables</li>
 * <li>Property Files</li>
 * <li>Default Value</li>
 * </ul>
 * 
 * <p>
 * When using {@link Configuration} {@code getValue()} functions to get a
 * configuration value, the {@code key} value is the one to use. Then the
 * configuration value will be retrieved, using the various key values.
 */
public record ConfigKey(String key, CmdLineArg commandLineArgument, String commandLineProperty,
		String environmentVariable, String configFileProperty, String defaultValue) {

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
	 * @param defaultValue        a {@code String} which represents an optional
	 *                            default value for the key.
	 */
	public ConfigKey {
		Objects.requireNonNull(key);
		if (Objects.isNull(commandLineArgument) && Utils.isNullOrEmpty(commandLineProperty)
				&& Utils.isNullOrEmpty(environmentVariable) && Utils.isNullOrEmpty(configFileProperty)
				&& defaultValue == null) {
			throw new ConfigurationException(
					"Config Keys require at least one of Command Line Argument, Command Line Property, Environment Variable, Configuration File Property or Default Value to be populated.");
		}
	}

	public static Builder newKeyBuilder(String key) {
		if (Utils.isNullOrEmpty(key)) {
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
			if (!Utils.isNullOrEmpty(commandLineArg)) {
				cmdLineArg = new CmdLineArg(commandLineArg, commandLineArgShort);
			}
			return new ConfigKey(key, cmdLineArg, commandLineProperty, envVariable, configFileProperty, defaultValue);
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
