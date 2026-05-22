package io.github.trquinn76.configuration;

/**
 * Defines a Command Line Argument.
 * 
 * If you wish your command line arguments to use the '-' character, such as
 * '-v', then it needs to be explicitly included in the {@code argument} and
 * optional {@code shortArgument}.
 * 
 * @param argument      a command line argument; When found on the command line,
 *                      the following value will be taken as the configured
 *                      value. (eg: -help)
 * @param shortArgument intended to be a short version of the above
 *                      {@code argument}. (eg: -h)
 */
public record CmdLineArg(String argument, String shortArgument) {

	/**
	 * Create an instance of {@link CmdLineArg} with the given argument and short
	 * argument.
	 * 
	 * @param argument      a command line argument; When found on the command line,
	 *                      the following value will be taken as the configured
	 *                      value. (eg: -help)
	 * @param shortArgument intended to be a short version of the above
	 *                      {@code argument}. (eg: -h)
	 */
	public CmdLineArg {
		if (Utils.isNullOrBlank(argument)) {
			throw new ConfigurationException("Require a command line argument.");
		}
	}

	/**
	 * Create an instance of {@link CmdLineArg} with the given argument, and NO
	 * short argument.
	 * 
	 * @param argument a command line argument; When found on the command line, the
	 *                 following value will be taken as the configured value. (eg:
	 *                 -help)
	 */
	public CmdLineArg(String argument) {
		this(argument, null);
	}
}
