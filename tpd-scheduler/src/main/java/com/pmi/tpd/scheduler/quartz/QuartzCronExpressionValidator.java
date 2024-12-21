package com.pmi.tpd.scheduler.quartz;

import java.text.ParseException;
import java.util.Locale;

import org.quartz.CronExpression;

import com.pmi.tpd.scheduler.cron.CronSyntaxException;
import com.pmi.tpd.scheduler.cron.ICronExpressionValidator;
import com.pmi.tpd.scheduler.util.QuartzParseExceptionMapper;

/**
 * @author Christophe Friederich
 * @since 1.0
 */
public class QuartzCronExpressionValidator implements ICronExpressionValidator {

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isValid(final String cronExpression) {
        return CronExpression.isValidExpression(cronExpression);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void validate(final String cronExpression) throws CronSyntaxException {
        try {
            CronExpression.validateExpression(cronExpression);
        } catch (final ParseException pe) {
            throw QuartzParseExceptionMapper.mapException(cronExpression.toUpperCase(Locale.US), pe);
        }
    }
}
