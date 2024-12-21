package com.pmi.tpd.startup;

import javax.inject.Inject;

import org.springframework.context.ApplicationEventPublisher;

import com.pmi.tpd.api.ApplicationConstants;
import com.pmi.tpd.api.Product;
import com.pmi.tpd.api.exec.ProgressImpl;
import com.pmi.tpd.core.lifecycle.StartupProgressEvent;
import com.pmi.tpd.spring.context.AbstractSmartLifecycle;

/**
 * When the server has started completely, update the progress bar and the message to inform the user has started
 * successfully.
 */
public class ServerLifecycle extends AbstractSmartLifecycle {

    /** */
    private final ApplicationEventPublisher eventPublisher;

    @Inject
    public ServerLifecycle(final ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    @Override
    public int getPhase() {
        return ApplicationConstants.LifeCycle.LIFECYCLE_PHASE_SERVER_READY;
    }

    @Override
    public void start() {
        eventPublisher.publishEvent(
            new StartupProgressEvent(this, new ProgressImpl(Product.getName() + " has started successfully", 100)));

        super.start();
    }

}
