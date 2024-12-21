package com.pmi.tpd.core.elasticsearch.task.event;

import javax.annotation.Nonnull;

import com.pmi.tpd.api.event.annotation.AsynchronousPreferred;

/**
 * Raised when a indexing fails.
 * <p>
 * This event is internally audited with a HIGH priority.
 * </p>
 *
 * @author Christophe Friederich
 * @since 1.3
 */
@AsynchronousPreferred
public class IndexingFailedEvent extends IndexingEndedEvent {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public IndexingFailedEvent(@Nonnull final Object source) {
        super(source);
    }
}
