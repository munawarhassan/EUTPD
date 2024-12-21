package com.pmi.tpd.startup.check;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pmi.tpd.core.startup.IHomePathLocator;
import com.pmi.tpd.startup.HomeDirectoryResolver;

/**
 * Attempts to find a app.home configured as a system property.
 *
 * @author Christophe Friederich
 * @since 1.0
 */
class SystemPropertyHomePathLocator implements IHomePathLocator {

    /** */
    private static final Logger LOGGER = LoggerFactory.getLogger(SystemPropertyHomePathLocator.class);

    @Override
    public String getHome() {
        try {
            return new HomeDirectoryResolver().resolve().getHome().getAbsolutePath();
        } catch (final SecurityException e) {
            // /CLOVER:OFF
            // Some app servers may restrict access.
            final String message = String.format("Unable to obtain application home from system property: %s.",
                e.getMessage());
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(message, e);
            } else {
                LOGGER.info(message);
            }
            return null;
            // /CLOVER:ON
        }
    }

    @Override
    public String getDisplayName() {
        return "System Property";
    }
}
