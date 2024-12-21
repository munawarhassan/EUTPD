package com.pmi.tpd.core.event.advisor.setup;

import javax.annotation.Nonnull;

import com.pmi.tpd.api.util.Assert;

/**
 * A default, empty implementation of {@link SetupConfig} which always indicates the application is setup.
 *
 * @author Christophe Friederich
 * @since 1.3
 */
public class DefaultSetupConfig implements ISetupConfig {

    /**
     * Always {@code true}.
     *
     * @return {@code true}
     */
    @Override
    public boolean isSetup() {
        return true;
    }

    /**
     * Always {@code false}.
     *
     * @param uri
     *            the URI of a web page
     * @return {@code false}
     */
    @Override
    public boolean isSetupPage(@Nonnull final String uri) {
        Assert.checkNotNull(uri, "uri"); // Impose the proper API semantics

        return false;
    }
}
