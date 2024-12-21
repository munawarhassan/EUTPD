package com.pmi.tpd.core.event.publisher;

import com.pmi.tpd.api.event.publisher.IEventPublisher;
import com.pmi.tpd.api.util.Assert;
import com.pmi.tpd.core.event.TestEvent;

/**
 * @author Christophe Friederich
 * @since 1.0
 */
public class EventPublisherAwared implements IEventPublisherAware {

    /** */
    private IEventPublisher eventPublisher;

    /**
     * {@inheritDoc}
     */
    @Override
    public void setEventPublisher(final IEventPublisher eventPublisher) {
        this.eventPublisher = Assert.notNull(eventPublisher);

    }

    /**
     * publish event.
     */
    public void publishEvent() {
        this.eventPublisher.publish(new TestEvent(this));
    }
}
