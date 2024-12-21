package com.pmi.tpd.scheduler.cron;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Clarifies the reason for failure when a cron expression cannot be parsed.
 *
 * @author Christophe Friederich
 * @since 1.3
 */
public enum ErrorCode {

    /**
     * The cron expression used multiple expressions for the day-of-month, at least one of which used the {@code L}
     * flag. This is not supported; the {@code L} flag can only be used when a single expression is given.
     */
    COMMA_WITH_LAST_DOM("You cannot use 'L' or 'W' with multiple day-of-month values."),

    /**
     * The cron expression used multiple expressions for the day-of-week, at least one of which used the {@code L} flag.
     * This is not supported; the {@code L} flag can only be used when a single expression is given.
     */
    COMMA_WITH_LAST_DOW("You cannot use 'L' with multiple day-of-week values."),

    /**
     * The cron expression used multiple expressions for the day-of-week, at least one of which used the {@code #} flag.
     * This is not supported; the {@code #} flag can only be used when a single expression is given.
     */
    COMMA_WITH_NTH_DOW("You cannot use '#' with multiple day-of-week values."),

    /**
     * The cron expression used multiple expressions for the day-of-month, at least one of which used the {@code W}
     * flag. This is not supported; the {@code W} flag can only be used when a single expression is given.
     */
    COMMA_WITH_WEEKDAY_DOM("You cannot use 'W' with multiple day-of-month values."),

    /**
     * The reason of the failure could not be determined. This indicates that the underlying cause is an unexpected
     * runtime exception and indicates a programming error in the cron expression parsing. The {@code value} should be
     * the message from {@code cause}.
     */
    INTERNAL_PARSER_FAILURE("Internal parser failure: ", ""),

    /**
     * The cron expression contained a character that is not legal in the cron expression syntax. The {@code value}
     * should be the illegal character that was encountered.
     */
    ILLEGAL_CHARACTER("Unexpected character: '", "'"),

    /**
     * The cron expression contained a question-mark (<code>?</code>) followed by another non-whitespace character. This
     * special value can only be used by itself in the day-of-month or day-of-week field; it cannot be combined with any
     * other value. The {@code value} should be the illegal character that was encountered.
     */
    ILLEGAL_CHARACTER_AFTER_QM("Illegal character after '?': '", "'"),

    /**
     * The cron expression contained a hash (<code>#</code>) in the day-of-week column that was not followed by an
     * integer value from {@code 1} to {@code 5}.
     */
    ILLEGAL_CHARACTER_AFTER_HASH("A numeric value between 1 and 5 must follow the '#' option."),

    /**
     * The cron expression contained a step interval (a value after {@code /}) that was followed by additional
     * characters in the same field expression. The step interval must be the last part of a value. The slash (
     * <code>/</code>) must be followed by an integer. After that, the only legal characters are whitespace or a comma (
     * <code>,</code>) to separate it from the next value for that field.
     */
    ILLEGAL_CHARACTER_AFTER_INTERVAL("Illegal character after '/': '", "'"),

    /**
     * The cron expression contained a slash (<code>/</code>) that was not followed by an integer value. A step interval
     * value must be specified, such as {@code /5} in the minutes column to indicate that the accept values are spaces
     * five minutes apart.
     */
    INVALID_STEP("The step interval character '/' must be followed by a positive integer."),

    /**
     * The cron expression contained an interval step for the day-of-month that was greater than or equal to {@code 31}.
     * Since this value is large enough to make it impossible to result in a second value with any starting value in any
     * month, it is not permitted.
     */
    INVALID_STEP_DAY_OF_MONTH("The step interval for day-of-month must be less than 31: ", ""),

    /**
     * The cron expression contained an interval step for the day-of-week that was greater than or equal to {@code 7}.
     * Since this value is large enough to make it impossible to result in a second value, it is not permitted.
     */
    INVALID_STEP_DAY_OF_WEEK("The step interval for day-of-week must be less than 7: ", ""),

    /**
     * The cron expression contained an interval step for the hour that was greater than or equal to {@code 24}. Since
     * this value is large enough to make it impossible to result in a second value, it is not permitted.
     */
    INVALID_STEP_HOUR("The step interval for hour must be less than 24: ", ""),

    /**
     * The cron expression contained an interval step for the month that was greater than or equal to {@code 12}. Since
     * this value is large enough to make it impossible to result in a second value, it is not permitted.
     */
    INVALID_STEP_MONTH("The step interval for month must be less than 12: ", ""),

    /**
     * The cron expression contained an interval step for the second or minute that was greater than or equal to
     * {@code 60}. Since this value is large enough to make it impossible to result in a second value, it is not
     * permitted.
     */
    INVALID_STEP_SECOND_OR_MINUTE("The step interval for second or minute must be less than 60: ", ""),

    /**
     * The cron expression contained a sequence of letters that appears to be meant as the name of a month or
     * day-of-week, but it was malformed. For example, {@code MO} would be invalid because it is too short.
     */
    INVALID_NAME("Invalid name: '", "'"),

    /**
     * The cron expression contained a sequence of letters that appears to be meant as the name of a month or
     * day-of-week, but it was specified in one of the other fields.
     */
    INVALID_NAME_FIELD("This field does not support names: '", "'"),

    /**
     * The cron expression contained a sequence that appears to be meant as a name-based range of a month or day-of-week
     * values, but the end value of the sequence was malformed. Examples include {@code 3-FRI} or {@code MON-4} or
     * {@code FEB-L}.
     */
    INVALID_NAME_RANGE("Cannot specify a range using month or day-of-week names unless a valid name is used"
            + " for both bounds."),

    /**
     * The cron expression contained a sequence of letters that appears to be meant as the name of a month, but the name
     * was not recognized. Only the three-letter English abbreviations for month names are supported, such as
     * {@code FEB} for February or {@code SEP} for September.
     */
    INVALID_NAME_MONTH("Invalid month name: '", "'"),

    /**
     * The cron expression contained a sequence of letters that appears to be meant as the name of a day-of-week, but
     * the name was not recognized. Only the three-letter English abbreviations for day-of-week names are supported,
     * such as {@code MON} for Monday or {@code THU} for Thursday. Note that {@code THR} is not accepted.
     */
    INVALID_NAME_DAY_OF_WEEK("Invalid day-of-week name: '", "'"),

    /**
     * The cron expression contained a number in the minute or second field that was outside the supported range of
     * {@code [0, 59]}.
     */
    INVALID_NUMBER_SEC_OR_MIN("The values for seconds and minutes must be from 0 to 59."),

    /**
     * The cron expression contained a number in the hour field that was outside the supported range of {@code [0, 23]}.
     */
    INVALID_NUMBER_HOUR("The values for hours must be from 0 to 23."),

    /**
     * The cron expression contained a number in the day-of-month field that was outside the supported range of
     * {@code [0, 31]}.
     */
    INVALID_NUMBER_DAY_OF_MONTH("The values for day-of-month must be from 1 to 31."),

    /**
     * The cron expression contained an {@code L-x} or {@code L-xW} expression where the value of {@code x} was more
     * than {@code 30}. Since {@code 31} or more days before the last day of the month is always in a different month,
     * such an expression could not possibly ever match.
     */
    INVALID_NUMBER_DAY_OF_MONTH_OFFSET("The offset from the last day day of the month must be no more than 30."),

    /**
     * The cron expression contained a number in the month field that was outside the supported range of {@code [1, 12]}
     * .
     */
    INVALID_NUMBER_MONTH("The values for month must be from 1 to 12."),

    /**
     * The cron expression contained a number in the day-of-week field that was outside the supported range of
     * {@code [1, 7]}.
     */
    INVALID_NUMBER_DAY_OF_WEEK("The values for day-of-week must be from 1 to 7."),

    /**
     * The cron expression contained a number in the year field that was outside the supported range of
     * {@code [1970, 2299]}.
     */
    // Note: Currently unused because Quartz does not consider this a problem until
    // it tries to find
    // the next matching date and gives up because it is more than 100 years in the
    // future. It is
    // not caught by the parser.
    INVALID_NUMBER_YEAR("The values for year must be from 1970 to 2299."),

    /**
     * The cron expression contains a range in the year field that gave the years in reverse order, such as
     * {@code 2036-2016}. Reversed ranges are permitted for the other fields and "wrap around" such that {@code NOV-FEB}
     * means November, December, January, or February. There is no really meaningful way to do that for the year ranges,
     * so that is not allowed for this field.
     */
    INVALID_NUMBER_YEAR_RANGE("Year ranges must specify the earlier year first."),

    /**
     * The cron expression contained a question-mark (<code>?</code>) in some field other than the day-of-month or
     * day-of-week. This special value cannot be used in any other field.
     */
    QM_CANNOT_USE_HERE("You can only use '?' for the day-of-month or the day-of-week."),

    /**
     * The cron expression contained a question-mark (<code>?</code>) for both the day-of-month and the day-of-week. One
     * of these fields must be specified, and it is likely that changing either one to {@code *} will give the intended
     * result.
     */
    QM_CANNOT_USE_FOR_BOTH_DAYS("You cannot specify '?' for both the day-of-month and the day-of-week."),

    /**
     * The cron expression specified values other than {@code ?} for both the day-of-month and the day-of-week. Exactly
     * one of these fields must be specified; the other must be disabled by giving {@code ?} as its value.
     */
    QM_MUST_USE_FOR_ONE_OF_DAYS("You must use '?' for either the day-of-month or day-of-week."),

    /**
     * The cron expression contained what looks like an {@code L} flag, but it is in a column that does not support it
     * or is not in a place that makes sense. For example, you cannot specify {@code L} for the hour field, and while
     * you could say {@code L-3} as the day-of-month to indicate the third-to-last day of the month, {@code 3-L} does
     * not make sense.
     */
    UNEXPECTED_TOKEN_FLAG_L("The 'L' option was used incorrectly."),

    /**
     * The cron expression contained what looks like a {@code W} flag, but it is in a column that does not support it or
     * is not in a place that makes sense. For example, you cannot specify {@code W} for the hour field, and while you
     * could say {@code 3W} as the day-of-month to indicate the weekday closest to the third day of the month,
     * {@code 3-W} does not make sense.
     */
    UNEXPECTED_TOKEN_FLAG_W("The 'W' option was used incorrectly."),

    /**
     * The cron expression contained what looks like a {@code #} flag, but it is in a column that does not support it or
     * is not in a place that makes sense. For example, you cannot specify {@code #} in the hour field, and while you
     * could say {@code MON#3} as the day-of-week to indicate the third Monday of the month, {@code #3} by itself does
     * not make sense.
     */
    UNEXPECTED_TOKEN_HASH("The '#' option was used incorrectly."),

    /**
     * The cron expression contained an invalid hyphen (<code>-</code>). This can only be used for ranges that provide
     * both a starting and ending value, such as {@code 5-8} or {@code MAY-AUG}, or in the special syntax {@code L-3}
     * that is available for the day-of-month field.
     */
    UNEXPECTED_TOKEN_HYPHEN("Ranges specified with '-' must have both a starting and ending value."),

    /**
     * The cron expression ended without specifying all of the required fields or ended in a way that would require more
     * characters. Only the year field is optional; all other fields must be included in the cron expression. This error
     * code can also be used if the cron expression ended in a way that would have required more characters to follow,
     * such as a comma (<code>,</code>) or hyphen (<code>-</code>).
     */
    UNEXPECTED_END_OF_EXPRESSION("Unexpected end of expression");

    /** */
    @Nonnull
    private final String message;

    /** */
    private final String suffix;

    ErrorCode(final @Nonnull String message) {
        this(message, null);
    }

    ErrorCode(final @Nonnull String message, @Nullable final String suffix) {
        this.message = message;
        this.suffix = suffix;
    }

    /**
     * Renders the standard message for this error code and offending value.
     */
    @Nonnull
    public String toMessage(@Nullable final String value) {
        if (suffix != null) {
            return message + value + suffix;
        }
        return message;
    }
}
