package com.pmi.tpd.core.elasticsearch.task.event;

import javax.annotation.Nonnull;

import com.pmi.tpd.api.event.BaseEvent;

/**
 * An abstract base class for constructing events related to indexing.
 * <p>
 * This event is internally audited with a LOW priority.
 * </p>
 *
 * @author Christophe Friederich
 * @since 1.3
 */
public abstract class IndexingEvent extends BaseEvent {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    protected IndexingEvent(@Nonnull final Object source) {
        super(source);
    }
}
