package com.pmi.tpd.core.context;

import static org.mockito.ArgumentMatchers.isA;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import com.pmi.tpd.api.event.publisher.IEventPublisher;
import com.pmi.tpd.api.lifecycle.config.ApplicationStartedEvent;
import com.pmi.tpd.api.lifecycle.config.ApplicationStoppedEvent;
import com.pmi.tpd.api.lifecycle.config.ApplicationStoppingEvent;
import com.pmi.tpd.testing.junit5.MockitoTestCase;

public class ContextConfigurationLifecycleTest extends MockitoTestCase {

    @Mock
    private IEventPublisher eventPublisher;

    @InjectMocks
    private ContextConfiguration lifecycle;

    @Test
    public void testLifecycle() {
        assertTrue(lifecycle.isAutoStartup(), "ConfigLifecycle should be auto-startup");
        assertFalse(lifecycle.isRunning(), "ConfigLifecycle should not be running");

        lifecycle.start();
        assertTrue(lifecycle.isRunning(), "ConfigLifecycle should be running");
        verify(eventPublisher).publish(isA(ApplicationStartedEvent.class));

        lifecycle.stop();
        assertFalse(lifecycle.isRunning(), "ConfigLifecycle should be stopped");
        verify(eventPublisher).publish(isA(ApplicationStoppingEvent.class));
        verify(eventPublisher).publish(isA(ApplicationStoppedEvent.class));
    }
}
