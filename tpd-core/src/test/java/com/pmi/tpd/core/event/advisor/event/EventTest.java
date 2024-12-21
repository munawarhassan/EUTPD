package com.pmi.tpd.core.event.advisor.event;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

import java.io.PrintStream;

import org.junit.jupiter.api.Test;

import com.pmi.tpd.api.event.advisor.event.Event;
import com.pmi.tpd.api.event.advisor.event.EventType;

public class EventTest {

    @Test
    public void testToStringDoesNotWriteToSystemOut() throws Exception {
        final PrintStream realOut = System.out;

        final PrintStream mockOut = mock(PrintStream.class);
        System.setOut(mockOut);
        try {
            final Event event = new Event(new EventType("foo", "bar"), "fubar");
            assertNotNull(event.toString());
        } finally {
            try {
                verifyNoInteractions(mockOut);
            } finally {
                System.setOut(realOut);
            }
        }
    }
}
