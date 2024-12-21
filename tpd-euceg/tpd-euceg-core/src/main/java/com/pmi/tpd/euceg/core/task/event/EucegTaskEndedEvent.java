package com.pmi.tpd.euceg.core.task.event;

import javax.annotation.Nonnull;

/**
 * An abstract base class for constructing events raised when a euceg task ends, successfully or not.
 *
 * @author Christophe Friederich
 * @since 1.3
 */
public abstract class EucegTaskEndedEvent extends EucegTaskEvent {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    protected EucegTaskEndedEvent(@Nonnull final Object source) {
        super(source);
    }
}
