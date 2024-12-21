package com.pmi.tpd.web.logback;

import java.io.File;

import com.pmi.tpd.startup.HomeDirectoryResolver;
import com.pmi.tpd.startup.check.HomeStartupCheck;

import ch.qos.logback.core.spi.ContextAwareBase;
import ch.qos.logback.core.spi.PropertyDefiner;

/**
 * Uses the {@link HomeStartupCheck} to resolve the home directory and returns a property pointing to it.
 * <p/>
 * Note: This {@code PropertyDefiner} does <i>not</i> create the home directory if it does not already exist. It is left
 * to code inside the logging framework to do that work.
 */
public class HomeDirectoryPropertyDefiner extends ContextAwareBase implements PropertyDefiner {

    /**
     * Resolves the home directory and returns its absolute path.
     *
     * @return the absolute path to the home directory
     * @throws IllegalStateException
     *                               Thrown if the calculated path already exists and is not a directory.
     */
    @Override
    public String getPropertyValue() {
        final File home = resolveHomeDirectory();
        if (home.exists() && !home.isDirectory()) {
            throw new IllegalStateException("[" + home.getAbsolutePath() + "] is not a directory");
        }

        return home.getAbsolutePath();
    }

    /**
     * @return Returns a {@link File} representing the location of home folder.
     */
    protected File resolveHomeDirectory() {
        return new HomeDirectoryResolver().resolve().getHome();
    }
}
