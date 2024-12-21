package com.pmi.tpd.core.audit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import com.pmi.tpd.api.audit.IAuditEntry;
import com.pmi.tpd.api.audit.Priority;
import com.pmi.tpd.core.event.audit.AuditEvent;
import com.pmi.tpd.testing.junit5.MockitoTestCase;

public class AuditEventListenerTest extends MockitoTestCase {

    @Mock
    private IAuditEntryLoggingService loggingService;

    @Mock(lenient = true)
    private AuditEvent event;

    @InjectMocks
    private AuditEventListener listener;

    @BeforeEach
    public void setUp() throws Exception {
        when(event.getEntry()).thenReturn(mock(IAuditEntry.class));
    }

    @Test
    public void testOnAuditEventNoneLevelEventNoneLogging() throws Exception {
        when(event.getPriority()).thenReturn(Priority.NONE);
        listener.setPriorityToLog(Priority.NONE.name());
        listener.onAuditEvent(event);
        verifyZeroInteractions(loggingService);
    }

    @Test
    public void testOnAuditEventHighLevelEventNoneLogging() throws Exception {
        when(event.getPriority()).thenReturn(Priority.HIGH);
        listener.setPriorityToLog(Priority.NONE.name());
        listener.onAuditEvent(event);
        verifyZeroInteractions(loggingService);
    }

    @Test
    public void testOnAuditEventSameLevels() throws Exception {
        when(event.getPriority()).thenReturn(Priority.HIGH);
        listener.setPriorityToLog(Priority.HIGH.name());
        listener.onAuditEvent(event);
        verify(loggingService).log(any(IAuditEntry.class));
    }

    @Test
    public void testOnAuditEventFilteredLowerLevel() throws Exception {
        when(event.getPriority()).thenReturn(Priority.MEDIUM);
        listener.setPriorityToLog(Priority.HIGH.name());
        listener.onAuditEvent(event);
        verifyZeroInteractions(loggingService);
    }

    @Test
    public void testOnAuditEventHigherLevel() throws Exception {
        when(event.getPriority()).thenReturn(Priority.HIGH);
        listener.setPriorityToLog(Priority.MEDIUM.name());
        listener.onAuditEvent(event);
        verify(loggingService).log(any(IAuditEntry.class));
    }

    @Test
    public void testSetPriorityToLogInMixedCaseShouldWork() throws Exception {
        when(event.getPriority()).thenReturn(Priority.HIGH);
        listener.setPriorityToLog(Priority.MEDIUM.name());
        listener.onAuditEvent(event);
        verify(loggingService).log(any(IAuditEntry.class));
    }

    @Test
    public void testSetPriorityToLogWithInvalidValueShouldSetPriorityToHIGH() throws Exception {
        when(event.getPriority()).thenReturn(Priority.HIGH);
        listener.setPriorityToLog("Invalid");
        listener.onAuditEvent(event);
        verify(loggingService).log(any(IAuditEntry.class));
    }

    @Test
    public void testSetPriorityToLogWithInvalidValueShouldIgnoreMEDIUMPriorityEvents() throws Exception {
        when(event.getPriority()).thenReturn(Priority.MEDIUM);
        listener.setPriorityToLog("Invalid");
        listener.onAuditEvent(event);
        verifyZeroInteractions(loggingService);
    }
}
