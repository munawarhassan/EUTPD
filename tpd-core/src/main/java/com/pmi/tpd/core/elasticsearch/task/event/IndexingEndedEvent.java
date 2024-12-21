package com.pmi.tpd.core.elasticsearch.task.event;

import javax.annotation.Nonnull;

/**
 * An abstract base class for constructing events raised when a indexing ends, successfully or not.
 *
 * @author Christophe Friederich
 * @since 1.3
 */
public abstract class IndexingEndedEvent extends IndexingEvent {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    protected IndexingEndedEvent(@Nonnull final Object source) {
        super(source);
    }
}
