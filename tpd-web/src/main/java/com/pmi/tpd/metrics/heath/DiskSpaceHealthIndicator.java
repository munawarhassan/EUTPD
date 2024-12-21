package com.pmi.tpd.metrics.heath;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A {@link HealthIndicator} that checks available disk space and reports a status of {@link Status#DOWN} when it drops
 * below a configurable threshold.
 *
 * @author devacfr<christophefriederich@mac.com>
 * @since 1.0
 */
public class DiskSpaceHealthIndicator extends AbstractHealthIndicator {

    /** */
    private static final Logger LOGGER = LoggerFactory.getLogger(DiskSpaceHealthIndicator.class);

    /** */
    private final File path;

    /** */
    private final long threshold;

    /**
     * Create a new {@code DiskSpaceHealthIndicator} instance.
     *
     * @param path
     *            the Path used to compute the available disk space
     * @param threshold
     *            the minimum disk space that should be available (in bytes)
     */
    public DiskSpaceHealthIndicator(final File path, final long threshold) {
        this.path = path;
        this.threshold = threshold;
    }

    @Override
    protected void doHealthCheck(final Health.Builder builder) throws Exception {
        final long diskFreeInBytes = this.path.getUsableSpace();
        if (diskFreeInBytes >= this.threshold) {
            builder.up();
        } else {
            LOGGER.warn(String.format("Free disk space below threshold. " + "Available: %d bytes (threshold: %d bytes)",
                diskFreeInBytes,
                this.threshold));
            builder.down();
        }
        builder.withDetail("total", this.path.getTotalSpace())
                .withDetail("free", diskFreeInBytes)
                .withDetail("type", "alert")
                .withDetail("product", "java")
                .withDetail("threshold", this.threshold);
    }

}