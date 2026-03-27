package io.github.trquinn76.configuration;

/**
 * A specialist {@link RuntimeException} for Configuration issues.
 */
@SuppressWarnings("serial")
public class ConfigurationException extends RuntimeException {

    public ConfigurationException() {
        super();
    }

    public ConfigurationException(String message, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public ConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }

    public ConfigurationException(String message) {
        super(message);
    }

    public ConfigurationException(Throwable cause) {
        super(cause);
    }

}
