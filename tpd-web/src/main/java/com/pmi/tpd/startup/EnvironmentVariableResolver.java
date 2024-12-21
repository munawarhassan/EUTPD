package com.pmi.tpd.startup;

/**
 * An utility class that wraps the access of environment variables for testing purposes.
 *
 * @author Christophe Friederich
 * @since 1.0
 */
public interface EnvironmentVariableResolver {

    /**
     * @param environmentVariable
     * @return
     */
    public String getValue(String environmentVariable);
}
