package com.pmi.tpd.spring.context;

import org.springframework.context.SmartLifecycle;

/**
 * A base class to simplify implementing Spring's {@code SmartLifecycle} interface.
 *
 * @since 1.3
 */
public abstract class AbstractSmartLifecycle implements SmartLifecycle {

    /** */
    private volatile boolean running;

    @Override
    public boolean isAutoStartup() {
        return true;
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    @Override
    public void start() {
        running = true;
    }

    @Override
    public void stop() {
        running = false;
    }

    @Override
    public void stop(final Runnable callback) {
        stop();

        callback.run();
    }
}
