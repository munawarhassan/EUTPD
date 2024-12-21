package com.pmi.tpd.core.event.advisor;

import org.junit.jupiter.api.Test;

import com.pmi.tpd.api.event.advisor.EventLevel;
import com.pmi.tpd.api.event.advisor.IEventAdvisorService;
import com.pmi.tpd.api.event.advisor.IEventContainer;
import com.pmi.tpd.api.event.advisor.event.Event;
import com.pmi.tpd.core.event.advisor.support.DefaultEventContainer;
import com.pmi.tpd.testing.junit5.TestCase;

public class EventsTest extends TestCase {

    private final IEventContainer eventContainer = new DefaultEventContainer();

    private final Event maintenanceEvent = new Event(null, null,
            new EventLevel(IEventAdvisorService.LEVEL_MAINTENANCE, "no comment"));

    private final Event errorEvent = new Event(null, null, new EventLevel(EventLevel.ERROR, "no comment"));

    @Test
    public void testFindHighestEventLevel() throws Exception {
        assertNull(EventAdvisorService.getInstance(eventContainer).findHighestEventLevel());

        eventContainer.publishEvent(errorEvent);
        assertEquals(EventLevel.ERROR, EventAdvisorService.getInstance(eventContainer).findHighestEventLevel());

        eventContainer.publishEvent(maintenanceEvent);
        assertEquals(EventLevel.ERROR, EventAdvisorService.getInstance(eventContainer).findHighestEventLevel());

        eventContainer.discardEvent(errorEvent);
        assertEquals(IEventAdvisorService.LEVEL_MAINTENANCE,
            EventAdvisorService.getInstance(eventContainer).findHighestEventLevel());
    }

    @Test
    public void testHasEventAtLeastAtLevel() throws Exception {
        assertFalse(
            EventAdvisorService.getInstance().isLevelAtLeast(IEventAdvisorService.LEVEL_MAINTENANCE, EventLevel.ERROR));
        assertTrue(EventAdvisorService.getInstance().isLevelAtLeast(EventLevel.ERROR, EventLevel.ERROR));
        assertTrue(EventAdvisorService.getInstance().isLevelAtLeast(EventLevel.FATAL, EventLevel.ERROR));
    }

    @Test
    public void testHasEventAtMostAtLevel() throws Exception {
        assertTrue(
            EventAdvisorService.getInstance().isLevelAtMost(IEventAdvisorService.LEVEL_MAINTENANCE, EventLevel.ERROR));
        assertTrue(EventAdvisorService.getInstance().isLevelAtMost(EventLevel.ERROR, EventLevel.ERROR));
        assertFalse(EventAdvisorService.getInstance().isLevelAtMost(EventLevel.FATAL, EventLevel.ERROR));
    }
}
