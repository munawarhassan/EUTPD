package com.pmi.tpd.scheduler.quartz.hazelcast;

import static com.google.common.base.Preconditions.checkNotNull;

import java.text.ParseException;
import java.util.TimeZone;

import javax.annotation.concurrent.Immutable;

import org.quartz.CronExpression;
import org.quartz.CronScheduleBuilder;
import org.quartz.CronTrigger;

/**
 * @author Christophe Friederich
 * @since 1.1
 */
@Immutable
public final class CronTriggerConfig extends AbstractTriggerConfig {

  /** */
  private static final long serialVersionUID = 1L;

  /** */
  private final String expression;

  /** */
  private final String timeZoneId;

  /**
   * private constructor.
   *
   * @param builder
   *                a builder.
   */
  private CronTriggerConfig(final Builder builder) {
    super(builder);

    expression = checkNotNull(builder.expression, "expression");
    timeZoneId = checkNotNull(builder.timeZoneId, "timeZoneId");
  }

  @Override
  public Builder copy() {
    return new Builder(this);
  }

  public String getExpression() {
    return expression;
  }

  public String getTimeZoneId() {
    return timeZoneId;
  }

  @SuppressWarnings("unchecked")
  @Override
  protected CronScheduleBuilder newScheduleBuilder() {
    CronExpression cron;
    try {
      cron = new CronExpression(expression);
    } catch (final ParseException e) {
      // This should never happen
      throw new IllegalStateException(e);
    }
    cron.setTimeZone(TimeZone.getTimeZone(timeZoneId));

    return CronScheduleBuilder.cronSchedule(cron);
  }

  /**
   * @author Christophe Friederich
   * @since 1.1
   */
  public static class Builder extends AbstractBuilder<Builder, CronTriggerConfig> {

    private String expression;

    private String timeZoneId;

    public Builder() {
    }

    public Builder(final CronTriggerConfig trigger) {
      super(trigger);

      expression(trigger.getExpression());
      timeZoneId(trigger.getTimeZoneId());
    }

    public Builder(final CronTrigger trigger) {
      super(trigger);

      expression(trigger.getCronExpression());
      timeZoneId(trigger.getTimeZone().getID());
    }

    @Override
    public CronTriggerConfig build() {
      return new CronTriggerConfig(this);
    }

    public Builder expression(final String value) {
      expression = value;

      return self();
    }

    public Builder timeZoneId(final String value) {
      timeZoneId = value;

      return self();
    }

    @Override
    protected Builder self() {
      return this;
    }
  }

}
