package com.pmi.tpd.cluster.concurrent;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import com.google.common.collect.Sets;
import com.pmi.tpd.testing.junit5.MockitoTestCase;

public class DefaultThreadLocalContextManagerTest extends MockitoTestCase {

    @Mock(lenient = true)
    private IStatefulService statefulService1;

    @Mock
    private ITransferableState state1;

    @Mock(lenient = true)
    private IStatefulService statefulService2;

    @Mock
    private ITransferableState state2;

    private DefaultThreadLocalContextManager manager;

    @BeforeEach
    public void setUp() throws Exception {
        when(statefulService1.getState()).thenReturn(state1);
        when(statefulService2.getState()).thenReturn(state2);

        manager = new DefaultThreadLocalContextManager();
        manager.setStatefulServices(statefulService1, statefulService2);
    }

    @Test
    public void testClearThreadLocalContext() throws Exception {
        // first set the current context
        manager.setThreadLocalContext(manager.getThreadLocalContext());
        // next clear it
        manager.clearThreadLocalContext();

        verify(state1).remove();
        verify(state2).remove();
    }

    @Test
    public void testGetThreadLocalContext() throws Exception {
        final CompositeTransferableState threadState = manager.getThreadLocalContext();

        // verify that each of the stateful services has been called to get the state from
        verify(statefulService1).getState();
        verify(statefulService2).getState();

        assertEquals(Sets.newHashSet(state1, state2), Sets.newHashSet(threadState.getStates()));
    }

    @Test
    public void testSetThreadLocalContext() throws Exception {
        manager.setThreadLocalContext(new CompositeTransferableState(Sets.newHashSet(state1, state2)));

        verify(state1).apply();
        verify(state2).apply();
    }

    @Test
    public void testSetThreadLocalContextInvalidInput() throws Exception {
        manager.setThreadLocalContext(new Object()); // no exceptions on invalid input
        manager.setThreadLocalContext(null); // Handles null
    }
}
