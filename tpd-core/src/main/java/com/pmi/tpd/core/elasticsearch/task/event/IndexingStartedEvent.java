package com.pmi.tpd.core.elasticsearch.task.event;

import javax.annotation.Nonnull;

import com.pmi.tpd.api.event.annotation.AsynchronousPreferred;

/**
 * Raised when the system starts creating a new index.
 *
 * @author Christophe Friederich
 * @since 1.3
 */
@AsynchronousPreferred
public class IndexingStartedEvent extends IndexingEvent {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public IndexingStartedEvent(@Nonnull final Object source) {
        super(source);
    }
}
