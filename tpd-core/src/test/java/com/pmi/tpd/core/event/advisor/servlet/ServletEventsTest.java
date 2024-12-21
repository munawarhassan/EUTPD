package com.pmi.tpd.core.event.advisor.servlet;

import org.junit.jupiter.api.Test;

import com.pmi.tpd.testing.junit5.TestCase;

public class ServletEventsTest extends TestCase {

    @Test
    public void testGetConfigThrowsWhenUninitialised() {
        assertThrows(IllegalArgumentException.class, () -> {
            ServletEventAdvisor.getInstance().getConfig();
        });
    }

    @Test
    public void testGetEventContainerThrowsWhenUninitialised() {
        assertThrows(IllegalArgumentException.class, () -> {
            ServletEventAdvisor.getInstance().getEventContainer();
        });
    }
}
