package com.pmi.tpd.web.logback.pattern;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.google.common.collect.ImmutableList;
import com.pmi.tpd.api.LoggingConstants;

import ch.qos.logback.classic.pattern.ClassicConverter;
import ch.qos.logback.classic.pattern.MDCConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Context;

/**
 * A simple wrapper around a chain of standard Logback {@code ClassicConverter}s which pulls various properties out of
 * the MDC and inserts them into the log message if they're present.
 * <p/>
 * The implementation here is designed with two goals:
 * <ol>
 * <li>Keep the format string concise by allowing us to programmatically add/remove MDC entries as desired</li>
 * <li>Eliminate spaces between entries that don't exist</li>
 * </ol>
 * Building a format string using a chain of {@code %X "someKey"}} entries results in a series of spaces when those
 * values are not defined. This implementation only appends spaces when it actually extracts a value from the MDC and
 * inserts it into the message.
 */
public class RequestContextConverter extends ClassicConverter {

    /** */
    private final List<MDCConverter> converters;

    /**
     * Create new instance of {@link RequestContextConverter}.
     */
    public RequestContextConverter() {
        converters = ImmutableList.<MDCConverter> builder()
                .add(converterFor(LoggingConstants.MDC_USERNAME))
                .add(converterFor(LoggingConstants.MDC_REQUEST_ID))
                .add(converterFor(LoggingConstants.MDC_SESSION_ID))
                .add(converterFor(LoggingConstants.MDC_REMOTE_ADDRESS))
                .add(converterFor(LoggingConstants.MDC_REQUEST_ACTION))
                .build();
    }

    @Override
    public String convert(final ILoggingEvent event) {
        final StringBuilder builder = new StringBuilder();
        for (final MDCConverter converter : converters) {
            final String converted = converter.convert(event);
            if (StringUtils.isNotEmpty(converted)) {
                if (builder.length() > 0) {
                    builder.append(" ");
                }
                builder.append(converted);
            }
        }

        return builder.toString();
    }

    @Override
    public void setContext(final Context context) {
        super.setContext(context);

        for (final MDCConverter converter : converters) {
            converter.setContext(context);
        }
    }

    @Override
    public void start() {
        super.start();

        for (final MDCConverter converter : converters) {
            converter.start();
        }
    }

    @Override
    public void stop() {
        super.stop();

        for (final MDCConverter converter : converters) {
            converter.stop();
        }
    }

    private static MDCConverter converterFor(final String key) {
        final MDCConverter converter = new MDCConverter();
        converter.setOptionList(Arrays.asList(key));

        return converter;
    }
}
