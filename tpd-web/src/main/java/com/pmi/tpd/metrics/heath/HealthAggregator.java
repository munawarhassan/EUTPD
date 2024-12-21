package com.pmi.tpd.metrics.heath;

import java.util.Map;

/**
 * Strategy interface used by {@link CompositeHealthIndicator} to aggregate {@link Health} instances into a final one.
 * <p>
 * This is especially useful to combine subsystem states expressed through {@link Health#getStatus()} into one state for
 * the entire system. The default implementation {@link OrderedHealthAggregator} sorts {@link Status} instances based on
 * a priority list.
 * <p>
 * It is possible to add more complex {@link Status} types to the system. In that case either the
 * {@link OrderedHealthAggregator} needs to be properly configured or users need to register a custom
 * {@link HealthAggregator} as bean.
 */
public interface HealthAggregator {

    /**
     * Aggregate several given {@link Health} instances into one.
     *
     * @param healths
     *            the health instances to aggregate
     * @return the aggregated health
     */
    Health aggregate(Map<String, Health> healths);
}
