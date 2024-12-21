package com.pmi.tpd.scheduler.util;

import static com.pmi.tpd.scheduler.cron.ErrorCode.COMMA_WITH_LAST_DOM;
import static com.pmi.tpd.scheduler.cron.ErrorCode.COMMA_WITH_LAST_DOW;
import static com.pmi.tpd.scheduler.cron.ErrorCode.COMMA_WITH_NTH_DOW;
import static com.pmi.tpd.scheduler.cron.ErrorCode.ILLEGAL_CHARACTER;
import static com.pmi.tpd.scheduler.cron.ErrorCode.ILLEGAL_CHARACTER_AFTER_HASH;
import static com.pmi.tpd.scheduler.cron.ErrorCode.ILLEGAL_CHARACTER_AFTER_INTERVAL;
import static com.pmi.tpd.scheduler.cron.ErrorCode.ILLEGAL_CHARACTER_AFTER_QM;
import static com.pmi.tpd.scheduler.cron.ErrorCode.INTERNAL_PARSER_FAILURE;
import static com.pmi.tpd.scheduler.cron.ErrorCode.INVALID_NAME;
import static com.pmi.tpd.scheduler.cron.ErrorCode.INVALID_NAME_DAY_OF_WEEK;
import static com.pmi.tpd.scheduler.cron.ErrorCode.INVALID_NAME_FIELD;
import static com.pmi.tpd.scheduler.cron.ErrorCode.INVALID_NAME_MONTH;
import static com.pmi.tpd.scheduler.cron.ErrorCode.INVALID_NAME_RANGE;
import static com.pmi.tpd.scheduler.cron.ErrorCode.INVALID_NUMBER_DAY_OF_MONTH;
import static com.pmi.tpd.scheduler.cron.ErrorCode.INVALID_NUMBER_DAY_OF_MONTH_OFFSET;
import static com.pmi.tpd.scheduler.cron.ErrorCode.INVALID_NUMBER_DAY_OF_WEEK;
import static com.pmi.tpd.scheduler.cron.ErrorCode.INVALID_NUMBER_HOUR;
import static com.pmi.tpd.scheduler.cron.ErrorCode.INVALID_NUMBER_MONTH;
import static com.pmi.tpd.scheduler.cron.ErrorCode.INVALID_NUMBER_SEC_OR_MIN;
import static com.pmi.tpd.scheduler.cron.ErrorCode.INVALID_NUMBER_YEAR_RANGE;
import static com.pmi.tpd.scheduler.cron.ErrorCode.INVALID_STEP;
import static com.pmi.tpd.scheduler.cron.ErrorCode.INVALID_STEP_DAY_OF_MONTH;
import static com.pmi.tpd.scheduler.cron.ErrorCode.INVALID_STEP_DAY_OF_WEEK;
import static com.pmi.tpd.scheduler.cron.ErrorCode.INVALID_STEP_HOUR;
import static com.pmi.tpd.scheduler.cron.ErrorCode.INVALID_STEP_MONTH;
import static com.pmi.tpd.scheduler.cron.ErrorCode.INVALID_STEP_SECOND_OR_MINUTE;
import static com.pmi.tpd.scheduler.cron.ErrorCode.QM_CANNOT_USE_FOR_BOTH_DAYS;
import static com.pmi.tpd.scheduler.cron.ErrorCode.QM_CANNOT_USE_HERE;
import static com.pmi.tpd.scheduler.cron.ErrorCode.QM_MUST_USE_FOR_ONE_OF_DAYS;
import static com.pmi.tpd.scheduler.cron.ErrorCode.UNEXPECTED_TOKEN_FLAG_L;
import static com.pmi.tpd.scheduler.cron.ErrorCode.UNEXPECTED_TOKEN_FLAG_W;
import static com.pmi.tpd.scheduler.cron.ErrorCode.UNEXPECTED_TOKEN_HASH;

import java.text.ParseException;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.pmi.tpd.scheduler.cron.CronSyntaxException;
import com.pmi.tpd.scheduler.cron.ErrorCode;

/**
 * Maps the various {@code ParseException} messages from Quartz to our more informative and translatable exceptions in
 * the {@link com.pmi.tpd.scheduler.cron.scheduler.cron.CronSyntaxException} family.
 * <p/>
 * In general, Quartz returns garbage for the error offset in the {@code ParseException}s that it throws. This is
 * because it has already pulled apart the cron expression with {@code StringTokenizer} and no longer knows the offset
 * within the original string. The damage is pretty much impossible to repair, so for most of these we simply discard
 * the known incorrect error offset. Correctly reporting {@code -1} to indicate that the offset isn't known is better
 * than giving an offset that is probably wrong anyway.
 *
 * @since 1.0
 */
public class QuartzParseExceptionMapper {

    /** */
    // mappers that get called by other mappers in certain special cases
    static final ExceptionMapper INVALID_NAME_MAPPER = new InvalidNameMapper();

    /** */
    static final ExceptionMapper INVALID_NAME_RANGE_MAPPER = ignoreValue(INVALID_NAME_RANGE);

    /** */
    static final ExceptionMapper GENERAL_PARSE_FAILURE_MAPPER = new GeneralParseFailureMapper();

    /** */
    static final ExceptionMapper UNEXPECTED_FLAG_L_MAPPER = ignoreValue(UNEXPECTED_TOKEN_FLAG_L);

    /** */
    static final ExceptionMapper UNEXPECTED_FLAG_W_MAPPER = ignoreValue(UNEXPECTED_TOKEN_FLAG_W);

    /** Mappers that do an exact string match and return a simple error code with no position. */
    private static final Map<String, ErrorCode> SIMPLE_MAPPERS = ImmutableMap.<String, ErrorCode> builder()
            .put("Support for specifying both a day-of-week AND a day-of-month parameter is not implemented.",
                QM_MUST_USE_FOR_ONE_OF_DAYS)
            .put("Support for specifying 'L' and 'LW' with other days of the month is not implemented",
                COMMA_WITH_LAST_DOM)
            .put("Support for specifying 'L' with other days of the week is not implemented", COMMA_WITH_LAST_DOW)
            .put("'?' can only be specfied for Day-of-Month or Day-of-Week.", // 'specfied' typo is in Quartz
                QM_CANNOT_USE_HERE)
            .put("'?' can only be specfied for Day-of-Month -OR- Day-of-Week.", // 'specfied' typo is in Quartz
                QM_CANNOT_USE_FOR_BOTH_DAYS)
            .put("Support for specifying multiple \"nth\" days is not imlemented.", // 'imlemented' typo is in Quartz
                COMMA_WITH_NTH_DOW)
            .put("Minute and Second values must be between 0 and 59", INVALID_NUMBER_SEC_OR_MIN)
            .put("Hour values must be between 0 and 23", INVALID_NUMBER_HOUR)
            .put("Day of month values must be between 1 and 31", INVALID_NUMBER_DAY_OF_MONTH)
            .put("Month values must be between 1 and 12", INVALID_NUMBER_MONTH)
            .put("Day-of-Week values must be between 1 and 7", INVALID_NUMBER_DAY_OF_WEEK)
            .put("Offset from last day must be <= 30", INVALID_NUMBER_DAY_OF_MONTH_OFFSET)
            .put("'/' must be followed by an integer.", INVALID_STEP)
            .put("A numeric value between 1 and 5 must follow the '#' option", ILLEGAL_CHARACTER_AFTER_HASH)
            .put("The 'W' option does not make sense with values larger than 31 (max number of days in a month)",
                INVALID_NUMBER_DAY_OF_MONTH)
            .put(
                "Illegal cron expression format (java.lang.IllegalArgumentException: "
                        + "Start year must be less than stop year)",
                INVALID_NUMBER_YEAR_RANGE)
            .build();

    /**
     * Mappers that do a prefix match. Note: order is significant here, as the first match wins.
     */
    private static final Map<String, ExceptionMapper> PREFIX_MAPPERS = ImmutableMap.<String, ExceptionMapper> builder()
            .put("Unexpected end of expression.", new UnexpectedEndOfExpressionMapper())
            .put("Invalid Day-of-Week value: '", new RemovePrefixAndSuffix("'", new InvalidDayOfWeekNameMapper()))
            .put("Invalid Month value: '", new RemovePrefixAndSuffix("'", new InvalidMonthNameMapper()))
            .put("Illegal character after '?': ", new SingleCharAfterPrefix(error(ILLEGAL_CHARACTER_AFTER_QM)))
            .put("Illegal characters for this position: '",
                new RemovePrefixAndSuffix("'", new IllegalCharactersMapper()))
            .put("Increment > 60 : ", new RemovePrefix(error(INVALID_STEP_SECOND_OR_MINUTE)))
            .put("Increment > 31 : ", new RemovePrefix(error(INVALID_STEP_DAY_OF_MONTH)))
            .put("Increment > 24 : ", new RemovePrefix(error(INVALID_STEP_HOUR)))
            .put("Increment > 7 : ", new RemovePrefix(error(INVALID_STEP_DAY_OF_WEEK)))
            .put("Increment > 12 : ", new RemovePrefix(error(INVALID_STEP_MONTH)))
            .put("Unexpected character: ", new SingleCharAfterPrefix(error(ILLEGAL_CHARACTER)))
            .put("Unexpected character '",
                new RemovePrefixAndSuffix("' after '/'", error(ILLEGAL_CHARACTER_AFTER_INTERVAL)))
            .put("'L' option is not valid here. (pos=", new RemovePrefixAndSuffix(")", UNEXPECTED_FLAG_L_MAPPER))
            .put("'W' option is not valid here. (pos=", new RemovePrefixAndSuffix(")", UNEXPECTED_FLAG_W_MAPPER))
            .put("'#' option is not valid here. (pos=",
                new RemovePrefixAndSuffix(")", ignoreValue(UNEXPECTED_TOKEN_HASH)))
            .put("Illegal cron expression format (java.lang.NumberFormatException: For input string: \"",
                new RemovePrefixAndSuffix("\")", new NumberFormatExceptionMapper()))
            .put(
                "Illegal cron expression format (java.lang.StringIndexOutOfBoundsException: "
                        + "String index out of range: ",
                new RemovePrefixAndSuffix(")", new StringIndexOutOfBoundsMapper()))
            .put("Illegal cron expression format (", new RemovePrefixAndSuffix(")", GENERAL_PARSE_FAILURE_MAPPER))
            .build();

    private QuartzParseExceptionMapper() {
        throw new UnsupportedOperationException(
                getClass().getName() + " is a mapper class and should not be instantiated");
    }

    /**
     * @param cronExpression
     * @param pe
     * @return
     */
    public static CronSyntaxException mapException(final String cronExpression, final ParseException pe) {
        final String message = pe.getMessage();
        if (message == null) {
            return mapGeneral(cronExpression, pe);
        }

        final ErrorCode errorCode = SIMPLE_MAPPERS.get(message);
        if (errorCode != null) {
            return CronSyntaxException.builder().cronExpression(cronExpression).errorCode(errorCode).cause(pe).build();
        }

        return mapExceptionByPrefix(cronExpression, pe);
    }

    private static CronSyntaxException mapExceptionByPrefix(final String cronExpression, final ParseException pe) {
        final String message = pe.getMessage();
        for (final Map.Entry<String, ExceptionMapper> entry : PREFIX_MAPPERS.entrySet()) {
            final String prefix = entry.getKey();
            if (message.startsWith(prefix)) {
                return entry.getValue().map(cronExpression, pe, prefix);
            }
        }

        return mapGeneral(cronExpression, pe);
    }

    private static CronSyntaxException mapGeneral(final String cronExpression, final ParseException pe) {
        Throwable cause = pe.getCause();
        if (cause == null) {
            cause = pe;
        }
        return CronSyntaxException.builder()
                .cronExpression(cronExpression)
                .errorCode(INTERNAL_PARSER_FAILURE)
                .cause(cause)
                .value(pe.getMessage())
                .build();
    }

    static boolean startsWithNumber(final String s) {
        if (s.isEmpty()) {
            return false;
        }
        final char c = s.charAt(0);
        return c >= '0' && c <= '9';
    }

    // The various strategies for mapping a ParseException to something meaningful
    static interface ExceptionMapper {

        CronSyntaxException map(String cronExpression, ParseException pe, String value);
    }

    static class RemovePrefix implements ExceptionMapper {

        private final ExceptionMapper delegate;

        RemovePrefix(final ExceptionMapper delegate) {
            this.delegate = delegate;
        }

        @Override
        public CronSyntaxException map(final String cronExpression, final ParseException pe, final String prefix) {
            return delegate.map(cronExpression, pe, pe.getMessage().substring(prefix.length()));
        }
    }

    static class SingleCharAfterPrefix implements ExceptionMapper {

        private final ExceptionMapper delegate;

        SingleCharAfterPrefix(final ExceptionMapper delegate) {
            this.delegate = delegate;
        }

        @Override
        public CronSyntaxException map(final String cronExpression, final ParseException pe, final String prefix) {
            final char c = pe.getMessage().charAt(prefix.length());
            return delegate.map(cronExpression, pe, String.valueOf(c));
        }
    }

    static class RemovePrefixAndSuffix implements ExceptionMapper {

        private final String suffix;

        private final ExceptionMapper delegate;

        RemovePrefixAndSuffix(final String suffix, final ExceptionMapper delegate) {
            this.suffix = suffix;
            this.delegate = delegate;
        }

        @Override
        public CronSyntaxException map(final String cronExpression, final ParseException pe, final String prefix) {
            final String value = pe.getMessage();

            final int pos = value.lastIndexOf(suffix);
            if (pos > prefix.length()) {
                return delegate.map(cronExpression, pe, value.substring(prefix.length(), pos));
            }

            // Hmmm... Do the best that we can with it, then...
            return delegate.map(cronExpression, pe, value.substring(prefix.length()));
        }
    }

    static class ErrorCodeMapper implements ExceptionMapper {

        private final ErrorCode errorCode;

        ErrorCodeMapper(final ErrorCode errorCode) {
            this.errorCode = errorCode;
        }

        @Override
        public CronSyntaxException map(final String cronExpression, final ParseException pe, final String value) {
            return CronSyntaxException.builder()
                    .cronExpression(cronExpression)
                    .errorCode(errorCode)
                    .cause(pe)
                    .value(value)
                    .build();
        }
    }

    static class IgnoreValue extends ErrorCodeMapper {

        IgnoreValue(final ErrorCode errorCode) {
            super(errorCode);
        }

        @Override
        public CronSyntaxException map(final String cronExpression, final ParseException pe, final String value) {
            return super.map(cronExpression, pe, null);
        }
    }

    static class UnexpectedEndOfExpressionMapper implements ExceptionMapper {

        @Override
        public CronSyntaxException map(final String cronExpression, final ParseException pe, final String value) {
            return CronSyntaxException.builder()
                    .cronExpression(cronExpression)
                    .errorCode(ErrorCode.UNEXPECTED_END_OF_EXPRESSION)
                    .errorOffset(cronExpression.length())
                    .cause(pe)
                    .build();
        }
    }

    /**
     * Quartz found something it thinks is a name in a position that doesn't allow names. We want to make it clearer
     * what the problem is than to just say it is illegal characters, but unfortunately we can also get this in the
     * day-of-month field when it contains a malformed {@code "L-"} expression, and there is a better error to report
     * for that case.
     */
    static class IllegalCharactersMapper implements ExceptionMapper {

        /** */
        private static final ExceptionMapper WRONG_FIELD = error(INVALID_NAME_FIELD);

        @Override
        public CronSyntaxException map(final String cronExpression, final ParseException pe, final String value) {
            final int hyphen = value.indexOf('-');
            final String s = hyphen != -1 ? value.substring(0, hyphen) : value;

            if ("L".equals(s)) {
                return UNEXPECTED_FLAG_L_MAPPER.map(cronExpression, pe, null);
            }

            if ("W".equals(s)) {
                return UNEXPECTED_FLAG_W_MAPPER.map(cronExpression, pe, null);
            }

            return WRONG_FIELD.map(cronExpression, pe, value);
        }
    }

    /**
     * Quartz hit a StringIndexOutOfBoundsException because it assumed a name was at least 3 characters long when it
     * wasn't.
     * <p/>
     * Since Quartz tokenizes the expression into fields before this happens, the error offset can't be trusted and we
     * don't really know where in the expression the error was detected. This uses regex to find the probable culprit,
     * which is going to be a sequence of letters that is 2 or shorter. The sequences "L", "W", and "LW" are valid in
     * some settings; we ignore them rather than try to get too fancy. If we can't figure out what's wrong, then a
     * general parser failure is reported. That is, after all, the real problem, here.
     */
    static class InvalidNameMapper implements ExceptionMapper {

        /** */
        private static final Pattern REGEX_FIND_NAMES = Pattern.compile("[A-Z]+");

        /** */
        private static final Pattern REGEX_FIND_BAD_FLAG_L = Pattern.compile("[^A-Z]L-( |\t|$)");

        /** */
        private static final Set<String> L_AND_W_FLAGS = ImmutableSet.of("L", "W", "LW");

        @Override
        public CronSyntaxException map(final String cronExpression, final ParseException pe, final String ignored) {
            Matcher matcher = REGEX_FIND_NAMES.matcher(cronExpression);
            while (matcher.find()) {
                final String name = matcher.group(0);
                if (name.length() < 3 && !L_AND_W_FLAGS.contains(name)) {
                    return CronSyntaxException.builder()
                            .cronExpression(cronExpression)
                            .errorCode(INVALID_NAME)
                            .errorOffset(matcher.start())
                            .value(name)
                            .cause(pe)
                            .build();
                }
            }

            matcher = REGEX_FIND_BAD_FLAG_L.matcher(cronExpression);
            if (matcher.find()) {
                return CronSyntaxException.builder()
                        .cronExpression(cronExpression)
                        .errorCode(UNEXPECTED_TOKEN_FLAG_L)
                        .errorOffset(matcher.start() + 1)
                        .cause(pe)
                        .build();
            }

            // Nothing that we recognize... oh well...
            return mapGeneral(cronExpression, pe);
        }
    }

    static class InvalidDayOfWeekNameMapper implements ExceptionMapper {

        /** */
        private static final ExceptionMapper BAD_DAY_OF_WEEK = error(INVALID_NAME_DAY_OF_WEEK);

        @Override
        public CronSyntaxException map(final String cronExpression, final ParseException pe, final String value) {
            // MON-4 => INVALID_NAME_RANGE instead of INVALID_NAME_DAY_OF_WEEK
            final ExceptionMapper mapper = startsWithNumber(value) ? INVALID_NAME_RANGE_MAPPER : BAD_DAY_OF_WEEK;
            return mapper.map(cronExpression, pe, value);
        }
    }

    static class InvalidMonthNameMapper implements ExceptionMapper {

        /** */
        private static final ExceptionMapper BAD_MONTH = error(INVALID_NAME_MONTH);

        @Override
        public CronSyntaxException map(final String cronExpression, final ParseException pe, final String value) {
            // FEB-4 => INVALID_NAME_RANGE instead of INVALID_NAME_MONTH
            final ExceptionMapper mapper = startsWithNumber(value) ? INVALID_NAME_RANGE_MAPPER : BAD_MONTH;
            return mapper.map(cronExpression, pe, value);
        }
    }

    // This happens when there is supposed to be a number and Quartz tries to parse it without checking.
    // Examples are in a range where the first half was numeric, like 4-XYZ, or the step interval, such
    // as 3-7/XYZ. We try to identify these two cases and report the correct specific error for them.
    static class NumberFormatExceptionMapper implements ExceptionMapper {

        /** */
        private static final Pattern REGEX_FIND_BAD_RANGE = Pattern.compile("[0-9]+-([A-Za-z]+)");

        /** */
        private static final Pattern REGEX_FIND_BAD_STEP = Pattern.compile("/[^0-9]");

        @Override
        public CronSyntaxException map(final String cronExpression, final ParseException pe, final String ignored) {
            final Matcher range = REGEX_FIND_BAD_RANGE.matcher(cronExpression);
            final Matcher step = REGEX_FIND_BAD_STEP.matcher(cronExpression);

            if (range.find()) {
                if (step.find() && step.start() < range.start()) {
                    return mapStep(cronExpression, pe, step);
                }
                return mapRange(cronExpression, pe, range);
            }

            if (step.find()) {
                return mapStep(cronExpression, pe, step);
            }

            // Nothing that we recognize... oh well...
            return mapGeneral(cronExpression, pe);
        }

        private static CronSyntaxException mapRange(final String cronExpression,
            final ParseException pe,
            final Matcher range) {
            return CronSyntaxException.builder()
                    .cronExpression(cronExpression)
                    .errorCode(INVALID_NAME_RANGE)
                    .errorOffset(range.start(1))
                    .cause(pe)
                    .build();
        }

        private static CronSyntaxException mapStep(final String cronExpression,
            final ParseException pe,
            final Matcher step) {
            return CronSyntaxException.builder()
                    .cronExpression(cronExpression)
                    .errorCode(INVALID_STEP)
                    .errorOffset(step.start() + 1)
                    .cause(pe)
                    .build();
        }
    }

    // The Quartz cron expression parser hits this when it's expecting an English name for a month or
    // day-of-week, but there aren't enough characters to complete the sequence (it doesn't bother to
    // check first). It can also hit it for "L-" by itself. Ugh!
    static class StringIndexOutOfBoundsMapper implements ExceptionMapper {

        @Override
        public CronSyntaxException map(final String cronExpression, final ParseException pe, final String value) {
            final int len = toInt(value);
            switch (len) {
                case 1:
                case 2:
                case 3:
                    return INVALID_NAME_MAPPER.map(cronExpression, pe, null);

                case 5:
                case 6:
                case 7:
                    return INVALID_NAME_RANGE_MAPPER.map(cronExpression, pe, null);
            }

            // Nothing that we recognize... oh well...
            return mapGeneral(cronExpression, pe);
        }
    }

    static class GeneralParseFailureMapper implements ExceptionMapper {

        @Override
        public CronSyntaxException map(final String cronExpression, final ParseException pe, final String value) {
            return mapGeneral(cronExpression, pe);
        }
    }

    static int toInt(final String s) {
        try {
            if (s != null) {
                return Integer.parseInt(s);
            }
        } catch (final NumberFormatException nfe) {
            // Doesn't matter; just make sure it won't match a known case
        }
        return -1;
    }

    private static ErrorCodeMapper error(final ErrorCode errorCode) {
        return new ErrorCodeMapper(errorCode);
    }

    private static IgnoreValue ignoreValue(final ErrorCode errorCode) {
        return new IgnoreValue(errorCode);
    }
}
