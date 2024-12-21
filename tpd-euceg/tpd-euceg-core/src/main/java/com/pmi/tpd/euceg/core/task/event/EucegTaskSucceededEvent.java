package com.pmi.tpd.euceg.core.task.event;

import javax.annotation.Nonnull;

import com.pmi.tpd.api.event.annotation.AsynchronousPreferred;

/**
 * Raised when a euceg task completes successfully.
 *
 * @author Christophe Friederich
 * @since 1.3
 */
@AsynchronousPreferred
public class EucegTaskSucceededEvent extends EucegTaskEndedEvent {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public EucegTaskSucceededEvent(@Nonnull final Object source) {
        super(source);
    }
}
