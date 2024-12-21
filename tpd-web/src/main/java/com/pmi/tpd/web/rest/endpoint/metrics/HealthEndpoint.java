package com.pmi.tpd.web.rest.endpoint.metrics;

import static com.pmi.tpd.api.util.Assert.notNull;

import java.util.Map;

import javax.annotation.Nonnull;

import com.pmi.tpd.metrics.heath.CompositeHealthIndicator;
import com.pmi.tpd.metrics.heath.Health;
import com.pmi.tpd.metrics.heath.HealthAggregator;
import com.pmi.tpd.metrics.heath.HealthIndicator;
import com.pmi.tpd.web.core.rs.endpoint.AbstractEndpoint;

/**
 * {@link Endpoint} to expose application health.
 */
public class HealthEndpoint extends AbstractEndpoint<Health> {

  /** */
  private final HealthIndicator healthIndicator;

  /**
   * Time to live for cached result, in milliseconds.
   */
  private long timeToLive = 1000;

  /**
   * Create a new {@link HealthIndicator} instance.
   *
   * @param healthAggregator
   *                         the health aggregator
   * @param healthIndicators
   *                         the health indicators
   */
  public HealthEndpoint(@Nonnull final HealthAggregator healthAggregator,
      @Nonnull final Map<String, HealthIndicator> healthIndicators) {
    super("health", false);
    notNull(healthAggregator, "healthAggregator");
    notNull(healthIndicators, "healthIndicators");
    final CompositeHealthIndicator healthIndicator = new CompositeHealthIndicator(healthAggregator);
    for (final Map.Entry<String, HealthIndicator> h : healthIndicators.entrySet()) {
      healthIndicator.addHealthIndicator(getKey(h.getKey()), h.getValue());
    }
    this.healthIndicator = healthIndicator;
  }

  /**
   * Gets Time to live for cached result. If accessed anonymously, we might need
   * to cache the result of this endpoint
   * to prevent a DOS attack.
   *
   * @return time to live in milliseconds (default 1000)
   */
  public long getTimeToLive() {
    return this.timeToLive;
  }

  /**
   * Sets Time to live for cached result.
   *
   * @param ttl
   *            a time to live in milliseconds (default 1000).
   */
  public void setTimeToLive(final long ttl) {
    this.timeToLive = ttl;
  }

  /**
   * Invoke all {@link HealthIndicator} delegates and collect their health
   * information.
   */
  @Override
  public Health invoke() {
    return this.healthIndicator.health();
  }

  /**
   * Turns the bean name into a key that can be used in the map of health
   * information.
   */
  private String getKey(final String name) {
    final int index = name.toLowerCase().indexOf("healthindicator");
    if (index > 0) {
      return name.substring(0, index);
    }
    return name;
  }
}
