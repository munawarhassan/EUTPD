package com.pmi.tpd.euceg.core.task.event;

import javax.annotation.Nonnull;

import com.pmi.tpd.api.event.annotation.AsynchronousPreferred;

/**
 * Raised when a euceg task is canceled by a system administrator.
 * <p>
 * This event is internally audited with a HIGH priority.
 * </p>
 *
 * @author Christophe Friederich
 * @since 1.3
 */
@AsynchronousPreferred
public class EucegTaskCanceledEvent extends EucegTaskEndedEvent {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public EucegTaskCanceledEvent(@Nonnull final Object source) {
        super(source);
    }
}
