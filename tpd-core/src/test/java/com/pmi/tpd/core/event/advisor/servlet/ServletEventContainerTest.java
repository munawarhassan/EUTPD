package com.pmi.tpd.core.event.advisor.servlet;

import java.util.Collection;

import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

import com.pmi.tpd.api.event.advisor.event.Event;
import com.pmi.tpd.api.event.advisor.event.EventType;
import com.pmi.tpd.core.event.advisor.support.DefaultEventContainer;
import com.pmi.tpd.testing.junit5.TestCase;

public class ServletEventContainerTest extends TestCase {

    @Test
    public void testContainer() {
        // no events at the start!
        final DefaultEventContainer container = new DefaultEventContainer();
        assertFalse(container.hasEvents());

        // add an event and check it exists
        final Event event = new Event(new EventType("systemic", "Systemic Anomaly"),
                "There is an anomaly in the matrix");
        container.publishEvent(event);
        assertTrue(container.hasEvents());
        final Collection<Event> containerEvents = container.getEvents();
        assertEquals(1, containerEvents.size());
        assertTrue(containerEvents.contains(event));

        // now remove the event and check it's gone
        container.discardEvent(event);
        assertFalse(container.hasEvents());
        assertEquals(0, container.getEvents().size());
    }

    @Test
    public void eventsReturnedInOrderOfAddition() {
        final DefaultEventContainer container = new DefaultEventContainer();

        for (int i = 0; i < 5; i++) {
            container.publishEvent(new Event(new EventType("", ""), Integer.toString(i)));
        }

        assertTrue(container.hasEvents());

        MatcherAssert.assertThat(container.getEvents(),
            Matchers.contains(eventWithDescription("0"),
                eventWithDescription("1"),
                eventWithDescription("2"),
                eventWithDescription("3"),
                eventWithDescription("4")));
    }

    @Test
    public void returnedEventsNotModifiable() {
        assertThrows(UnsupportedOperationException.class, () -> {
            new DefaultEventContainer().getEvents().add(new Event(new EventType("", ""), ""));
        });
    }

    private static Matcher<Event> eventWithDescription(final String s) {
        return new FeatureMatcher<>(Matchers.equalTo(s), "an Event with description", "description") {

            @Override
            protected String featureValueOf(final Event actual) {
                return actual.getDesc();
            }
        };
    }
}
