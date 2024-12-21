package com.pmi.tpd.startup;

import java.io.File;
import java.io.IOException;

import javax.annotation.Nonnull;

import org.springframework.util.Assert;

import com.google.common.base.Supplier;
import com.pmi.tpd.api.ApplicationConstants;

/**
 * Utility class to resolve the location of the home directory.
 *
 * @author Christophe Friederich
 * @since 1.0
 */
public class HomeDirectoryResolver {

    // TODO: create Listener to initialize
    // static {
    // // Unfortunately Spring evaluates custom XML attributes extremely early, so this can't be defined in
    // // HazelcastConfigFactoryBean or many Hazelcast classes create their loggers before it's set
    // System.setProperty("hazelcast.logging.type", "slf4j"); // Because Hazelcast has its own logging abstraction
    // }

    /** */
    static final String HOME_DIR_ENV_VARIABLE = ApplicationConstants.HOME_ENV_VARIABLE;

    /** */
    static final String SHARED_HOME_DIR_ENV_VARIABLE = ApplicationConstants.SHARED_HOME_DIR_ENV_VARIABLE;

    /** */
    private final EnvironmentVariableResolver environmentVariableResolver;

    /**
     *
     */
    public HomeDirectoryResolver() {
        this(new DefaultEnvironmentVariableResolver());
    }

    /**
     * @param environmentVariableResolver
     */
    public HomeDirectoryResolver(@Nonnull final EnvironmentVariableResolver environmentVariableResolver) {
        this.environmentVariableResolver = environmentVariableResolver;
    }

    /**
     * @return
     */
    public HomeDirectoryDetails resolve() {
        final File home = getHomeDirectory();
        final File sharedHome = getSharedHomeDirectory(home);
        return new HomeDirectoryDetails(home, sharedHome);
    }

    private File getHomeDirectory() {
        final String home = getPropertyValue(ApplicationConstants.PropertyKeys.HOME_PATH_SYSTEM_PROPERTY,
            HOME_DIR_ENV_VARIABLE,
            null);

        Assert.state(home != null,
            String.format(
                "No home directory is defined using either the system property '%s' or the environment variable '%s'",
                ApplicationConstants.PropertyKeys.HOME_PATH_SYSTEM_PROPERTY,
                HOME_DIR_ENV_VARIABLE));
        checkTilde(home, "home");

        return resolve(home);
    }

    private File getSharedHomeDirectory(final File home) {
        final String sharedHome = getPropertyValue(ApplicationConstants.PropertyKeys.SHARED_DIR_PATH_SYSTEM_PROPERTY,
            SHARED_HOME_DIR_ENV_VARIABLE,
            new Supplier<String>() {

                @Override
                public String get() {
                    return new File(home, "shared").getAbsolutePath();
                }
            });

        Assert.state(sharedHome != null, "Cannot derive shared home value from home value");
        checkTilde(sharedHome, "shared home");

        return resolve(sharedHome);
    }

    private String getPropertyValue(final String propertyName,
        final String environmentName,
        final Supplier<String> missingValueSupplier) {
        String propertyValue = System.getProperty(propertyName);
        if (propertyValue != null) {
            return propertyValue;
        }

        propertyValue = System.getProperty(environmentName);
        if (propertyValue != null) {
            return propertyValue;
        }

        propertyValue = environmentVariableResolver.getValue(environmentName);

        if (propertyValue == null && missingValueSupplier != null) {
            propertyValue = missingValueSupplier.get();
        }

        if (propertyValue != null) {
            System.setProperty(propertyName, propertyValue);
        }

        return propertyValue;
    }

    private void checkTilde(final String text, final String directoryName) {
        if (text != null && text.startsWith("~")) { // We're not a shell; we don't support tilde expansion
            throw new IllegalStateException("The " + directoryName + " directory [" + text + "] is invalid; "
                    + "tilde expansion is not supported. "
                    + "Please use an absolute path referring to a specific home directory.");
        }
    }

    private static File resolve(final String dirName) {
        final File dir = new File(dirName);
        try {
            return dir.getCanonicalFile();
        } catch (final IOException e) {
            return dir.getAbsoluteFile();
        }
    }
}
