package com.pmi.tpd.core.event.advisor.support;

import static com.pmi.tpd.api.util.Assert.checkNotNull;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.annotation.Nonnull;

import com.pmi.tpd.api.event.advisor.IEventContainer;
import com.pmi.tpd.api.event.advisor.event.Event;

/**
 * @author Christophe Friederich
 * @since 1.3
 */
public final class DefaultEventContainer implements IEventContainer {

    /** */
    private final List<Event> events = new CopyOnWriteArrayList<Event>();

    /**
     *
     */
    public DefaultEventContainer() {
    }

    /**
     * Adds the provided event to the collection.
     *
     * @param event
     *            the event to add
     */
    @Override
    public void publishEvent(@Nonnull final Event event) {
        events.add(checkNotNull(event, "event"));
    }

    /**
     * Retrieves an <i>unmodifiable</i> view of the current {@link Event} list.
     *
     * @return an unmodifiable collection of zero or more events
     */
    @Override
    @Nonnull
    public List<Event> getEvents() {
        return Collections.unmodifiableList(events);
    }

    /**
     * Retrieves a flag indicating whether there are any {@link Event}s in the list.
     *
     * @return {@link true} if the event list is not empty; otherwise, {@code false}
     */
    @Override
    public boolean hasEvents() {
        return !events.isEmpty();
    }

    /**
     * Removes the provided {@link Event} from the list, if it can be found.
     *
     * @param event
     *            the event to remove
     */
    @Override
    public void discardEvent(@Nonnull final Event event) {
        events.remove(checkNotNull(event, "event"));
    }
}
