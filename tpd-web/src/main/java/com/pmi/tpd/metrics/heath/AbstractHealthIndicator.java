package com.pmi.tpd.metrics.heath;

import com.codahale.metrics.health.HealthCheck;
import com.pmi.tpd.metrics.heath.Health.Builder;

/**
 * Base {@link HealthIndicator} implementations that encapsulates creation of {@link Health} instance and error
 * handling.
 * <p>
 * This implementation is only suitable if an {@link Exception} raised from {@link #doHealthCheck(Builder)} should
 * create a {@link Status#DOWN} health status.
 */
public abstract class AbstractHealthIndicator extends HealthCheck implements HealthIndicator {

    @Override
    public final Health health() {
        final Health.Builder builder = new Health.Builder();
        try {
            doHealthCheck(builder);
        } catch (final Exception ex) {
            builder.down(ex);
        }
        return builder.build();
    }

    @Override
    protected Result check() throws Exception {
        final Health health = health();
        if (health.getStatus() != Status.UP) {
            return Result.unhealthy(health.getStatus().getDescription(), health.getDetails());
        }
        return Result.healthy(health.getStatus().getDescription(), health.getDetails());
    }

    /**
     * Actual health check logic.
     *
     * @param builder
     *            the {@link Builder} to report health status and details
     * @throws Exception
     *             any {@link Exception} that should create a {@link Status#DOWN} system status.
     */
    protected abstract void doHealthCheck(Health.Builder builder) throws Exception;
}
