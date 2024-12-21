package com.pmi.tpd.web.logback;

import com.pmi.tpd.api.LoggingConstants;

import ch.qos.logback.core.spi.ContextAwareBase;
import ch.qos.logback.core.spi.PropertyDefiner;

/**
 * Defines the format used for the profile log.
 * 
 * @author Christophe Friederich
 * @since 1.0
 */
public class ProfileLogFormatPropertyDefiner extends ContextAwareBase implements PropertyDefiner {

    @Override
    public String getPropertyValue() {
        return LoggingConstants.PROFILE_LOG_FORMAT;
    }
}
