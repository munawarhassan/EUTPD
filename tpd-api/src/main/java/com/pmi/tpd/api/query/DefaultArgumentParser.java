package com.pmi.tpd.api.query;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default implementation of {@linkplain ArgumentParser}. Supported types are String, Integer, Long, Float, Boolean,
 * Enum and Date. If neither one of them match, it tries to invoke valueOf(String s) method via reflection on the type's
 * class.
 *
 * @author Christophe Friederich
 * @since 1.0
 */
public class DefaultArgumentParser implements IArgumentParser {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultArgumentParser.class.getName());

    private static final String DATE_PATTERN = "yyyy-MM-dd"; // ISO 8601

    private static final String DATE_TIME_PATTERN = "yyyy-MM-dd'T'HH:mm:ss"; // ISO 8601

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public <T> T parse(final String argument, final Class<T> type)
            throws ArgumentFormatException, IllegalArgumentException {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Parsing argument ''{}'' as type {}, thread {}",
                new Object[] { argument, type.getSimpleName(), Thread.currentThread().getName() });
        }

        // Nullable object
        if (argument == null || "null".equals(argument.trim().toLowerCase())) {
            return null;
        }

        // common types
        try {
            if (type.equals(String.class)) {
                return (T) argument;
            }
            if (type.equals(Integer.class) || type.equals(int.class)) {
                return (T) Integer.valueOf(argument);
            }
            if (type.equals(Boolean.class) || type.equals(boolean.class)) {
                return (T) Boolean.valueOf(argument);
            }
            if (type.isEnum()) {
                try {
                    return (T) Enum.valueOf((Class<Enum>) type, argument);
                } catch (final Exception ex) {
                    // jaxb enum specific with number
                    try {
                        final Method m = type.getMethod("fromValue", Integer.TYPE);
                        return (T) m.invoke(null, Integer.valueOf(argument));
                    } catch (NoSuchMethodException | SecurityException | IllegalAccessException
                            | InvocationTargetException e) {
                        throw ex;
                    }
                }

            }
            if (type.equals(Float.class) || type.equals(float.class)) {
                return (T) Float.valueOf(argument);
            }
            if (type.equals(Double.class) || type.equals(double.class)) {
                return (T) Double.valueOf(argument);
            }
            if (type.equals(Long.class) || type.equals(long.class)) {
                return (T) Long.valueOf(argument);
            }
            if (type.equals(BigDecimal.class)) {
                return (T) new BigDecimal(argument);
            }
        } catch (final IllegalArgumentException ex) {
            throw new ArgumentFormatException(argument, type, ex);
        }

        // date
        if (type.equals(Date.class)) {
            return (T) parseDate(argument, type);
        }

        if (type.equals(DateTime.class)) {
            return (T) parseDateTime(argument, type);
        }

        // try to parse via valueOf(String s) method
        try {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Trying to get and invoke valueOf(String s) method on {}", type);
            }
            final Method method = type.getMethod("valueOf", String.class);
            return (T) method.invoke(type, argument);
        } catch (final InvocationTargetException ex) {
            throw new ArgumentFormatException(argument, type, ex);
        } catch (final ReflectiveOperationException ex) {
            LOGGER.warn("{} does not have method valueOf(String s) or method is inaccessible", type);
            throw new IllegalArgumentException("Cannot parse argument type " + type);
        }
    }

    private <T> Date parseDate(final String argument, final Class<T> type) {
        try {
            return new SimpleDateFormat(DATE_TIME_PATTERN).parse(argument);
        } catch (final ParseException ex) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Not a date time format, lets try with date format.");
            }
        }
        try {
            return new SimpleDateFormat(DATE_PATTERN).parse(argument);
        } catch (final ParseException ex1) {
            throw new ArgumentFormatException(argument, type, ex1);
        }
    }

    private <T> DateTime parseDateTime(final String argument, final Class<T> type) {
        try {
            return DateTimeFormat.forPattern(DATE_TIME_PATTERN).parseDateTime(argument);
        } catch (final Throwable ex) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Not a date time format, lets try with date format.");
            }
        }
        try {
            final DateTimeFormatter fmt = ISODateTimeFormat.dateTime();
            return fmt.parseDateTime(argument);
        } catch (final Throwable ex1) {
            throw new ArgumentFormatException(argument, type, ex1);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> List<T> parse(final List<String> arguments, final Class<T> type)
            throws ArgumentFormatException, IllegalArgumentException {
        final List<T> castedArguments = new ArrayList<T>(arguments.size());
        for (final String argument : arguments) {
            castedArguments.add(this.parse(argument, type));
        }
        return castedArguments;
    }
}
