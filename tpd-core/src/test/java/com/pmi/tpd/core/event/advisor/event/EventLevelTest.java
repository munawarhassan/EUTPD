package com.pmi.tpd.core.event.advisor.event;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.pmi.tpd.api.event.advisor.EventLevel;
import com.pmi.tpd.core.event.advisor.EventAdvisorService;
import com.pmi.tpd.testing.junit5.TestCase;

public class EventLevelTest extends TestCase {

    @BeforeAll
    public static void initialize() {
        EventAdvisorService.initialize(getResourceAsStream(EventLevelTest.class, "test-event-config.xml"));
    }

    @AfterAll
    public static void terminate() {
        EventAdvisorService.getInstance().terminate();
    }

    @Test
    public void testEventLevel() {
        final EventLevel level = new EventLevel("foo", "bar");
        assertEquals("foo", level.getLevel());
        assertEquals("bar", level.getDescription());
    }

    @Test
    public void testGetEventLevel() {
        final EventLevel expectedWarning = new EventLevel("warning", "This is a warning buddy");
        assertEquals(expectedWarning, EventAdvisorService.getInstance().getEventLevel("warning").get());
    }
}
