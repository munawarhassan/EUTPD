package com.pmi.tpd.metrics.heath;

/**
 * Strategy interface used to provide an indication of application health.
 */
public interface HealthIndicator {

    /**
     * @return an indication of health
     */
    Health health();

}
