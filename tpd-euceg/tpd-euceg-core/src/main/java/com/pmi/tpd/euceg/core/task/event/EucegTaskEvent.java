package com.pmi.tpd.euceg.core.task.event;

import javax.annotation.Nonnull;

import com.pmi.tpd.api.event.BaseEvent;

/**
 * An abstract base class for constructing events related to euceg task.
 * <p>
 * This event is internally audited with a LOW priority.
 * </p>
 *
 * @author Christophe Friederich
 * @since 1.3
 */
public abstract class EucegTaskEvent extends BaseEvent {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    protected EucegTaskEvent(@Nonnull final Object source) {
        super(source);
    }
}
