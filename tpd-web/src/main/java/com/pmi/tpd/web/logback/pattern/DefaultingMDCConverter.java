package com.pmi.tpd.web.logback.pattern;

import org.apache.commons.lang3.StringUtils;

import ch.qos.logback.classic.pattern.MDCConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;

/**
 * A customization of the standard {@link MDCConverter} that returns a '-' for an empty MDC variable. For non-empty MDC
 * variables, the behaviour of the standard {@link MDCConverter} applies.
 *
 * @since 1.2.2
 */
public class DefaultingMDCConverter extends MDCConverter {

    @Override
    public String convert(final ILoggingEvent event) {
        return StringUtils.defaultIfEmpty(super.convert(event), "-");
    }
}
