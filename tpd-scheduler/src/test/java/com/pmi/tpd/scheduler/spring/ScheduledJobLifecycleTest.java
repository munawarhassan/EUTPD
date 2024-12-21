package com.pmi.tpd.scheduler.spring;

import static org.mockito.ArgumentMatchers.same;

import java.util.concurrent.Future;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.springframework.core.Ordered;

import com.google.common.collect.ImmutableList;
import com.pmi.tpd.api.scheduler.IScheduledJobSource;
import com.pmi.tpd.api.scheduler.ISchedulerService;
import com.pmi.tpd.api.scheduler.SchedulerServiceException;
import com.pmi.tpd.testing.junit5.MockitoTestCase;

public class ScheduledJobLifecycleTest extends MockitoTestCase {

    @Mock(name = "bad", extraInterfaces = Ordered.class, lenient = true)
    private IScheduledJobSource bad;

    @Mock
    private Future<?> future;

    @Mock(name = "good", extraInterfaces = Ordered.class, lenient = true)
    private IScheduledJobSource good;

    private ScheduledJobLifecycle lifecycle;

    @Mock
    private ISchedulerService schedulerService;

    @BeforeEach
    public void setup() throws Exception {
        doThrow(SchedulerServiceException.class).when(bad).schedule(same(schedulerService));
        doThrow(SchedulerServiceException.class).when(bad).unschedule(same(schedulerService));

        // Always run bad first when both sources are present
        trainOrder(good, Ordered.LOWEST_PRECEDENCE);
        trainOrder(bad, 0);

        lifecycle = new ScheduledJobLifecycle(schedulerService, ImmutableList.of(good));
    }

    @Test
    public void testLifecycle() throws Exception {
        assertFalse(lifecycle.isAutoStartup(), "ScheduledJobLifecycle should not be auto-startup");
        assertFalse(lifecycle.isRunning(), "ScheduledJobLifecycle should not be running");

        lifecycle.start();
        assertTrue(lifecycle.isRunning(), "ScheduledJobLifecycle should be running");
        verify(good).schedule(same(schedulerService));

        lifecycle.stop();
        assertFalse(lifecycle.isRunning(), "ScheduledJobLifecycle should be stopped");
        verify(good).unschedule(same(schedulerService));
    }

    @Test
    public void testStartSwallowsExceptions() throws Exception {
        new ScheduledJobLifecycle(schedulerService, ImmutableList.of(good, bad)).start();

        final InOrder ordered = inOrder(bad, good);
        ordered.verify(bad).schedule(same(schedulerService));
        ordered.verify(good).schedule(same(schedulerService));
    }

    @Test
    public void testStopSwallowsExceptions() throws Exception {
        reset(good);
        trainOrder(good, Ordered.HIGHEST_PRECEDENCE); // Ensure good runs "first" (remember, reverse unscheduling)

        new ScheduledJobLifecycle(schedulerService, ImmutableList.of(bad, good)).stop();

        final InOrder ordered = inOrder(bad, good);
        ordered.verify(bad).unschedule(same(schedulerService));
        ordered.verify(good).unschedule(same(schedulerService));
    }

    @Test
    public void testStopWithCallback() throws Exception {
        final Runnable callback = mock(Runnable.class);
        lifecycle.stop(callback);

        final InOrder ordered = inOrder(callback, good);
        ordered.verify(good).unschedule(same(schedulerService));
        ordered.verify(callback).run();
    }

    private static void trainOrder(final Object mock, final int order) {
        when(((Ordered) mock).getOrder()).thenReturn(order);
    }
}
