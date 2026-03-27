package io.github.trquinn76.configuration;

/**
 * Utility functions for use in {@link Configuration}.
 */
public class Utils {

	/**
	 * Returns true if the given {@code String} is null or blank.
	 * 
	 * @param str the {@code String} to test.
	 * @return true if the given {@code String} is null or blank, false otherwise.
	 */
	public static boolean isNullOrEmpty(String str) {
		return str == null || str.isBlank();
	}
}
