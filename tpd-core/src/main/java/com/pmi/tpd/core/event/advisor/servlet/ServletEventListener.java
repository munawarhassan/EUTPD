package com.pmi.tpd.core.event.advisor.servlet;

import javax.annotation.Nonnull;
import javax.servlet.ServletContext;

import com.pmi.tpd.api.event.advisor.event.AddEvent;
import com.pmi.tpd.api.event.advisor.event.Event;
import com.pmi.tpd.api.event.advisor.event.RemoveEvent;
import com.pmi.tpd.api.event.annotation.EventListener;

/**
 * A simple listener which may be used with the Events framework to listen for {@link AddEvent add} and
 * {@link RemoveEvent remove} events on the bus, allowing simplified interaction with.
 *
 * @author Christophe Friederich
 * @since 1.3
 */
public class ServletEventListener {

    /** */
    private final ServletContext servletContext;

    /**
     * @param servletContext
     */
    public ServletEventListener(@Nonnull final ServletContext servletContext) {
        this.servletContext = servletContext;
    }

    /**
     * Adds the {@link Event} wrapped by the provided {@link AddEvent} to the container.
     *
     * @param e
     *            the add event
     */
    @EventListener
    public void onAdd(@Nonnull final AddEvent e) {
        ServletEventAdvisor.getInstance().getEventContainer(servletContext).publishEvent(e.getEvent());
    }

    /**
     * Attempts to remove the {@link Event} wrapped by the provided {@link RemoveEvent} from the container.
     *
     * @param e
     *            the remove event
     */
    @EventListener
    public void onRemove(@Nonnull final RemoveEvent e) {
        ServletEventAdvisor.getInstance().getEventContainer(servletContext).discardEvent(e.getEvent());
    }
}
