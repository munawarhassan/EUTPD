package com.pmi.tpd.startup;

public class DefaultEnvironmentVariableResolver implements EnvironmentVariableResolver {

    @Override
    public String getValue(final String environmentVariable) {
        return System.getenv(environmentVariable);
    }
}
