package com.pmi.tpd.core.elasticsearch.task.event;

import javax.annotation.Nonnull;

import com.pmi.tpd.api.event.annotation.AsynchronousPreferred;

/**
 * Raised when a indexing completes successfully.
 *
 * @author Christophe Friederich
 * @since 1.3
 */
@AsynchronousPreferred
public class IndexingSucceededEvent extends IndexingEndedEvent {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public IndexingSucceededEvent(@Nonnull final Object source) {
        super(source);
    }
}
