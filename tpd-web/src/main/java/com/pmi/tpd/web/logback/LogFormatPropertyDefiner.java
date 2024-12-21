package com.pmi.tpd.web.logback;

import java.util.Map;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.CoreConstants;
import ch.qos.logback.core.spi.ContextAwareBase;
import ch.qos.logback.core.spi.PropertyDefiner;

import com.google.common.collect.Maps;
import com.pmi.tpd.api.LoggingConstants;
import com.pmi.tpd.web.logback.pattern.DefaultingMDCConverter;
import com.pmi.tpd.web.logback.pattern.EnhancedThrowableConverter;
import com.pmi.tpd.web.logback.pattern.RequestContextConverter;

/**
 * Defines the standard format to be used in the majority of log files.
 * <p/>
 * Ultimately, this property could be just as well defined in {@code logback.xml} without this class. The reason this
 * class exists is to bind custom converters into Logback so that specialised properties can be used in the format to to
 * produce files in the standard log format. It also gives us the ability to use code constants rather than relying on
 * keeping plain text consistent across the {@code logback.xml} file and the codebase.
 *
 * @see LoggingConstants
 */
public class LogFormatPropertyDefiner extends ContextAwareBase implements PropertyDefiner {

    @Override
    public String getPropertyValue() {
        return LoggingConstants.LOG_FORMAT;
    }

    @Override
    public void setContext(final Context context) {
        // Logback supports a standard extension point for adding our own custom
        // converters
        @SuppressWarnings("unchecked")
        Map<String, String> converters = (Map<String, String>) context.getObject(CoreConstants.PATTERN_RULE_REGISTRY);
        if (converters == null) {
            converters = Maps.newHashMap();

            context.putObject(CoreConstants.PATTERN_RULE_REGISTRY, converters);
        }

        // Add a custom converter for dealing with empty MDC variables
        converters.put(LoggingConstants.FORMAT_DEFAULTING_MDC, DefaultingMDCConverter.class.getName());
        // Add a custom converter to insert request details
        converters.put(LoggingConstants.FORMAT_REQUEST_CONTEXT, RequestContextConverter.class.getName());
        // Add a custom converter to insert a specially enhanced version of a
        // throwable
        converters.put(LoggingConstants.FORMAT_ENHANCED_THROWABLE, EnhancedThrowableConverter.class.getName());

        super.setContext(context);
    }
}
