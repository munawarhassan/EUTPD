package com.pmi.tpd.scheduler.cron;

/**
 * @author Christophe Friederich
 * @since 1.3
 */
public interface ICronExpressionValidator {

    /**
     * Returns {@code true} if the cron expression can be parsed successfully.
     * <p>
     * This is equivalent to calling {@link #validate(String)} except that it returns a boolean value as opposed to
     * throwing an exception when the expression is not valid.
     * </p>
     *
     * @param cronExpression
     *            the cron expression to be considered
     * @return {@code true} if the cron expression can be parsed successfully; {@code false} otherwise.
     */
    boolean isValid(String cronExpression);

    /**
     * Validates that a cron expression can be successfully parsed.
     *
     * @param cronExpression
     *            the cron expression to be considered
     * @throws CronSyntaxException
     *             if the cron expression contains invalid syntax
     */
    void validate(String cronExpression) throws CronSyntaxException;
}
