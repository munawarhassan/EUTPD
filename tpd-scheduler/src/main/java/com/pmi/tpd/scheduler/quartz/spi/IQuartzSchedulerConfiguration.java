package com.pmi.tpd.scheduler.quartz.spi;

import java.util.Properties;

import javax.annotation.Nonnull;

import com.pmi.tpd.scheduler.quartz.QuartzDefaultSettingsFactory;
import com.pmi.tpd.scheduler.spi.ISchedulerServiceConfiguration;

/**
 * Provides custom configuration settings for the Quartz 2.x scheduler service.
 *
 * @author Christophe Friederich
 * @since 1.0
 */
public interface IQuartzSchedulerConfiguration extends ISchedulerServiceConfiguration {

  /**
   * Returns custom properties for configuring the scheduler that will be used to
   * run local jobs.
   * <p>
   * <strong>WARNING</strong>: Since v1.3, the only property that provides a
   * default value is
   * {@code org.quartz.scheduler.skipUpdateCheck}, which is always forced to
   * {@code true} regardless of the settings
   * returned here. To get the old defaults, you can use
   * {@link QuartzDefaultSettingsFactory#getDefaultLocalSettings()} as the
   * starting point.
   * </p>
   */
  @Nonnull
  Properties getLocalSettings();

  /**
   * Returns custom properties for configuring the scheduler that will be used to
   * run clustered jobs.
   * <p>
   * <strong>WARNING</strong>: Since v1.3, the only property that provides a
   * default value is
   * {@code org.quartz.scheduler.skipUpdateCheck}, which is always forced to
   * {@code true} regardless of the settings
   * returned here. To get the old defaults, you can use
   * {@link QuartzDefaultSettingsFactory#getDefaultClusteredSettings()} as the
   * starting point.
   * </p>
   */
  @Nonnull
  Properties getClusteredSettings();
}
