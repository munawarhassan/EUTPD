package com.pmi.tpd.core.event.advisor.event;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.pmi.tpd.api.event.advisor.event.EventType;
import com.pmi.tpd.core.event.advisor.EventAdvisorService;
import com.pmi.tpd.core.event.advisor.config.ConfigurationEventException;
import com.pmi.tpd.testing.junit5.TestCase;

public class EventTypeTest extends TestCase {

    @BeforeAll
    public static void initialize() {
        EventAdvisorService.initialize(getResourceAsStream(EventTypeTest.class, "test-event-config.xml"));
    }

    @AfterAll
    public static void terminate() {
        EventAdvisorService.getInstance().terminate();
    }

    @Test
    public void testEventType() {
        final EventType type = new EventType("foo", "bar");
        assertEquals("foo", type.getType());
        assertEquals("bar", type.getDescription());
    }

    @Test
    public void testGetEventType() throws ConfigurationEventException {
        final EventType expectedWarning = new EventType("database", "Database");
        assertEquals(expectedWarning, EventAdvisorService.getInstance().getEventType("database").get());
    }
}
