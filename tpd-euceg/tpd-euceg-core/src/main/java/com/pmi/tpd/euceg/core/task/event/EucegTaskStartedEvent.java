package com.pmi.tpd.euceg.core.task.event;

import javax.annotation.Nonnull;

import com.pmi.tpd.api.event.annotation.AsynchronousPreferred;

/**
 * Raised when the system starts creating a euceg task.
 *
 * @author Christophe Friederich
 * @since 1.3
 */
@AsynchronousPreferred
public class EucegTaskStartedEvent extends EucegTaskEvent {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public EucegTaskStartedEvent(@Nonnull final Object source) {
        super(source);
    }
}
