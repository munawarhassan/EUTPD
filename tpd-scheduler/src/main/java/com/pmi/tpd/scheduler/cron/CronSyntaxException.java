package com.pmi.tpd.scheduler.cron;

import static com.google.common.base.MoreObjects.firstNonNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.pmi.tpd.api.scheduler.SchedulerServiceException;

/**
 * Signals that there is a syntax error in a given cron expression.
 *
 * @since 1.3
 */
public class CronSyntaxException extends SchedulerServiceException {

    /** */
    private static final long serialVersionUID = 5594187147397941674L;

    /** */
    @Nonnull
    private final ErrorCode errorCode;

    /** */
    @Nonnull
    private final String cronExpression;

    /** */
    private final String value;

    /** */
    private final int errorOffset;

    /**
     * Internal constructor. Use a {@link #builder() builder} to construct these.
     */
    @SuppressWarnings("null")
    CronSyntaxException(final Builder builder) {
        super(builder.toMessage());
        this.errorCode = firstNonNull(builder.errorCode, ErrorCode.INTERNAL_PARSER_FAILURE);
        this.cronExpression = firstNonNull(builder.cronExpression, "");
        this.value = builder.value;
        this.errorOffset = builder.errorOffset;
        if (builder.cause != null) {
            initCause(builder.cause);
        }
    }

    /**
     * Returns the error code identifying the underlying cause of the parse failure.
     *
     * @return the error code identifying the underlying cause of the parse failure.
     */
    @Nonnull
    public ErrorCode getErrorCode() {
        return errorCode;
    }

    /**
     * Returns the cron expression that could not be parsed.
     *
     * @return the cron expression that could not be parsed.
     */
    @Nonnull
    public String getCronExpression() {
        return cronExpression;
    }

    /**
     * Returns the value that caused the exception to be thrown, if that information is available. For example, if the
     * day-of-month field specified {@code 4,10-13,42-47}, then the value would be {@code "42"} because {@code 31} is
     * the maximum value permitted for that field.
     *
     * @return the value that caused the exception to be thrown, or {@code null} if that is irrelevant to the error or
     *         if the cause is unknown
     */
    @Nullable
    public String getValue() {
        return value;
    }

    /**
     * Returns the {@code 0}-based index of the character at which the parse error was identified.
     *
     * @return the {@code 0}-based index of the character at which the parse error was identified, or {@code -1} if the
     *         index is not known.
     */
    public int getErrorOffset() {
        return errorOffset;
    }

    /**
     * Returns a builder for constructing a {@code CronSyntaxExpression}.
     *
     * @return a builder for constructing a {@code CronSyntaxExpression}.
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * A builder for constructing a {@link com.pmi.tpd.scheduler.cron.scheduler.cron.CronSyntaxException}.
     */
    public static class Builder {

        /** */
        private String cronExpression;

        /** */
        private String value;

        /** */
        private ErrorCode errorCode;

        /** */
        private int errorOffset = -1;

        /** */
        private Throwable cause;

        Builder() {
        }

        /**
         * Sets the cron expression that caused the exception. If left unspecified, then {@code ""} is used.
         */
        public Builder cronExpression(@Nullable final String cronExpression) {
            this.cronExpression = cronExpression;
            return this;
        }

        /**
         * Sets the value that caused the exception. For example, if the month field contained {@code FEB-XYZ}, then
         * {@code "XYZ"} should be set for the value.
         */
        public Builder value(@Nullable final String value) {
            this.value = value;
            return this;
        }

        /**
         * Sets the value that caused the exception. This convenience method is equivalent to {@link #value(String)
         * value(String.valueOf(value))}.
         */
        public Builder value(final char value) {
            this.value = String.valueOf(value);
            return this;
        }

        /**
         * Sets the error code for the exception. If left unspecified, then {@link ErrorCode#INTERNAL_PARSER_FAILURE} is
         * used.
         */
        public Builder errorCode(@Nullable final ErrorCode errorCode) {
            this.errorCode = errorCode;
            return this;
        }

        /**
         * Sets the error code for the exception. If left unspecified, then {@code -1} is used.
         */
        public Builder errorOffset(final int errorOffset) {
            this.errorOffset = errorOffset >= 0 ? errorOffset : -1;
            return this;
        }

        /**
         * Sets the cause of the exception.
         */
        public Builder cause(@Nullable final Throwable cause) {
            this.cause = cause;
            return this;
        }

        /**
         * Returns the completed exception.
         *
         * @return the completed exception.
         */
        public CronSyntaxException build() {
            return new CronSyntaxException(this);
        }

        @Nonnull
        String toMessage() {
            return errorCode.toMessage(value);
        }
    }
}
