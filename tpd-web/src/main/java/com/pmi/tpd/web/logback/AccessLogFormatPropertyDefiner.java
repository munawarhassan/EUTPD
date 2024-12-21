package com.pmi.tpd.web.logback;

import com.pmi.tpd.api.LoggingConstants;

import ch.qos.logback.core.spi.ContextAwareBase;
import ch.qos.logback.core.spi.PropertyDefiner;

/**
 * Defines the format used for the access log.
 *
 * @author Christophe Friederich
 * @see LoggingConstants
 * @since 1.0
 */
public class AccessLogFormatPropertyDefiner extends ContextAwareBase implements PropertyDefiner {

    @Override
    public String getPropertyValue() {
        return LoggingConstants.ACCESS_LOG_FORMAT;
    }
}
