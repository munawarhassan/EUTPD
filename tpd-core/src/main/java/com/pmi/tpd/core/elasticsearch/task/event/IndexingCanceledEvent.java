package com.pmi.tpd.core.elasticsearch.task.event;

import javax.annotation.Nonnull;

import com.pmi.tpd.api.event.annotation.AsynchronousPreferred;

/**
 * Raised when a indexing is canceled by a system administrator.
 * <p>
 * This event is internally audited with a HIGH priority.
 * </p>
 *
 * @author Christophe Friederich
 * @since 1.3
 */
@AsynchronousPreferred
public class IndexingCanceledEvent extends IndexingEndedEvent {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public IndexingCanceledEvent(@Nonnull final Object source) {
        super(source);
    }
}
