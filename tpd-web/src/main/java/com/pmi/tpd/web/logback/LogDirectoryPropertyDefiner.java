package com.pmi.tpd.web.logback;

import java.io.File;

import org.apache.commons.lang3.StringUtils;

import com.pmi.tpd.api.ApplicationConstants.Directories;

import ch.qos.logback.core.spi.ContextAwareBase;
import ch.qos.logback.core.spi.PropertyDefiner;

/**
 * Uses the {@code home.dir} property from the underlying logging {@code Context} to define the
 * {@link com.pmi.tpd.core.ApplicationConstants.Directories.Directories#LOG_DIRECTORY log directory} within the home
 * directory.
 *
 * @see HomeDirectoryPropertyDefiner
 */
public class LogDirectoryPropertyDefiner extends ContextAwareBase implements PropertyDefiner {

    /**
     * Produces an absolute path to a {@link com.pmi.tpd.core.ApplicationConstants.Directories.Directories#LOG_DIRECTORY
     * log directory} under the home directory.
     *
     * @return the absolute path to the directory where log files should be placed
     * @throws IllegalStateException
     *             Thrown if the {@code home.dir} property is not defined, or if the calculated path path already exists
     *             and is not a directory.
     */
    @Override
    public String getPropertyValue() {
        final String homePath = getContext().getProperty("home.dir");
        if (StringUtils.isEmpty(homePath)) {
            throw new IllegalStateException("The \"home.dir\" property is not defined");
        }

        final File log = new File(homePath, Directories.LOG_DIRECTORY);
        if (log.exists() && !log.isDirectory()) {
            throw new IllegalStateException("[" + log.getAbsolutePath() + "] is not a directory");
        }
        return log.getAbsolutePath();
    }
}
