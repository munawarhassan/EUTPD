package com.pmi.tpd.metrics.heath;

import java.util.Set;

import com.codahale.metrics.jvm.ThreadDeadlockDetector;

public class ThreadDeadlockHealthCheck extends AbstractHealthIndicator {

    private final ThreadDeadlockDetector detector;

    /**
     * Creates a new health check.
     */
    public ThreadDeadlockHealthCheck() {
        this(new ThreadDeadlockDetector());
    }

    /**
     * Creates a new health check with the given detector.
     *
     * @param detector
     *            a thread deadlock detector
     */
    public ThreadDeadlockHealthCheck(final ThreadDeadlockDetector detector) {
        this.detector = detector;
    }

    @Override
    protected void doHealthCheck(final Health.Builder builder) throws Exception {
        final Set<String> threads = detector.getDeadlockedThreads();
        if (threads.isEmpty()) {
            builder.up().withDetail("type", "thread").withDetail("product", "java");
        } else {
            builder.down().withDetail("threads", threads.toString());
        }
    }

    @Override
    protected Result check() throws Exception {
        final Set<String> threads = detector.getDeadlockedThreads();
        if (threads.isEmpty()) {
            return Result.healthy();
        }
        return Result.unhealthy(threads.toString());
    }
}
