package com.pmi.tpd.euceg.core.task.event;

import javax.annotation.Nonnull;

import com.pmi.tpd.api.event.annotation.AsynchronousPreferred;

/**
 * Raised when a euceg task fails.
 * <p>
 * This event is internally audited with a HIGH priority.
 * </p>
 *
 * @author Christophe Friederich
 * @since 1.3
 */
@AsynchronousPreferred
public class EucegTaskFailedEvent extends EucegTaskEndedEvent {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public EucegTaskFailedEvent(@Nonnull final Object source) {
        super(source);
    }
}
