package com.pmi.tpd.testing.query.util;

public class LinearBackoff implements Backoff {

    private final long interval;

    public LinearBackoff(final long interval) {
        this.interval = interval;
    }

    @Override
    public void yield() throws InterruptedException {
        if (interval > 0) {
            Thread.sleep(interval);
        }
    }

    @Override
    public void yield(final long bound) throws InterruptedException {
        if (interval > 0) {
            Thread.sleep(Math.min(bound, interval));
        }
    }

    @Override
    public void resetBackoff() {

    }
}
