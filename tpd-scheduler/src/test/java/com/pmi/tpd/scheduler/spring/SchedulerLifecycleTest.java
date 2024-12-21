package com.pmi.tpd.scheduler.spring;

import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.Mock;

import com.pmi.tpd.api.scheduler.ILifecycleAwareSchedulerService;
import com.pmi.tpd.api.scheduler.SchedulerServiceException;
import com.pmi.tpd.testing.junit5.MockitoTestCase;

public class SchedulerLifecycleTest extends MockitoTestCase {

    private static final int SHUTDOWN_TIMEOUT = 15;

    @Mock
    private ILifecycleAwareSchedulerService schedulerService;

    private SchedulerLifecycle schedulerLifecycle;

    @BeforeEach
    public void setup() {
        schedulerLifecycle = new SchedulerLifecycle(schedulerService, SHUTDOWN_TIMEOUT);
    }

    @Test
    public void testLifecycle() throws Exception {
        assertTrue(schedulerLifecycle.isAutoStartup(), "SchedulerLifecycle should be auto-startup");
        assertFalse(schedulerLifecycle.isRunning(), "SchedulerLifecycle should not be running");

        schedulerLifecycle.start();
        assertTrue(schedulerLifecycle.isRunning(), "SchedulerLifecycle should be running");
        verify(schedulerService).start();

        schedulerLifecycle.stop();
        assertFalse(schedulerLifecycle.isRunning(), "SchedulerLifecycle should be stopped");
        verify(schedulerService).shutdown();
    }

    @Test
    public void testStartPropagatesExceptions() throws Exception {
        assertThrows(IllegalStateException.class, () -> {
            doThrow(SchedulerServiceException.class).when(schedulerService).start();
            schedulerLifecycle.start();
        });
    }

    @Test
    public void testStopSchedulerService() throws Exception {
        when(schedulerService.waitUntilIdle(SHUTDOWN_TIMEOUT, TimeUnit.SECONDS)).thenReturn(true);
        schedulerLifecycle.stop();

        final InOrder ordered = inOrder(schedulerService);
        ordered.verify(schedulerService).shutdown();
        ordered.verify(schedulerService).waitUntilIdle(SHUTDOWN_TIMEOUT, TimeUnit.SECONDS);
    }

    @Test
    public void testStopSchedulerServiceWaitUntilIdleTimeout() throws Exception {
        when(schedulerService.waitUntilIdle(SHUTDOWN_TIMEOUT, TimeUnit.SECONDS)).thenReturn(false);
        schedulerLifecycle.stop();

        final InOrder ordered = inOrder(schedulerService);
        ordered.verify(schedulerService).shutdown();
        ordered.verify(schedulerService).waitUntilIdle(SHUTDOWN_TIMEOUT, TimeUnit.SECONDS);
    }

    @Test
    public void testStopSchedulerServiceInterruptedWhileWaiting() throws Exception {
        doThrow(new InterruptedException("FAILED")).when(schedulerService)
                .waitUntilIdle(SHUTDOWN_TIMEOUT, TimeUnit.SECONDS);

        schedulerLifecycle.stop();

        final InOrder ordered = inOrder(schedulerService);
        ordered.verify(schedulerService).shutdown();
        ordered.verify(schedulerService).waitUntilIdle(SHUTDOWN_TIMEOUT, TimeUnit.SECONDS);
    }

    @Test
    public void testStopWithCallback() throws Exception {
        final Runnable callback = mock(Runnable.class);
        schedulerLifecycle.stop(callback);

        final InOrder ordered = inOrder(schedulerService, callback);
        ordered.verify(schedulerService).shutdown();
        ordered.verify(callback).run();
    }
}
