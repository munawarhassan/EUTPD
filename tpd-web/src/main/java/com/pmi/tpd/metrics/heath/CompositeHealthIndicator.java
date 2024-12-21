package com.pmi.tpd.metrics.heath;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.util.Assert;

/**
 * {@link HealthIndicator} that returns health indications from all registered delegates.
 */
public class CompositeHealthIndicator implements HealthIndicator {

    private final Map<String, HealthIndicator> indicators;

    private final HealthAggregator healthAggregator;

    /**
     * Create a new {@link CompositeHealthIndicator}.
     *
     * @param healthAggregator
     *            the health aggregator
     */
    public CompositeHealthIndicator(final HealthAggregator healthAggregator) {
        this(healthAggregator, new LinkedHashMap<String, HealthIndicator>());
    }

    /**
     * Create a new {@link CompositeHealthIndicator} from the specified indicators.
     *
     * @param healthAggregator
     *            the health aggregator
     * @param indicators
     *            a map of {@link HealthIndicator}s with the key being used as an indicator name.
     */
    public CompositeHealthIndicator(final HealthAggregator healthAggregator,
            final Map<String, HealthIndicator> indicators) {
        Assert.notNull(healthAggregator, "HealthAggregator must not be null");
        Assert.notNull(healthAggregator, "Indicators must not be null");
        this.indicators = new LinkedHashMap<String, HealthIndicator>(indicators);
        this.healthAggregator = healthAggregator;
    }

    public void addHealthIndicator(final String name, final HealthIndicator indicator) {
        this.indicators.put(name, indicator);
    }

    @Override
    public Health health() {
        final Map<String, Health> healths = new LinkedHashMap<String, Health>();
        for (final Map.Entry<String, HealthIndicator> entry : this.indicators.entrySet()) {
            healths.put(entry.getKey(), entry.getValue().health());
        }
        return this.healthAggregator.aggregate(healths);
    }

}
