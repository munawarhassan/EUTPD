package com.pmi.tpd.core.maintenance;

import static org.mockito.ArgumentMatchers.isA;

import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import com.pmi.tpd.api.event.advisor.event.AddEvent;
import com.pmi.tpd.api.event.advisor.event.RemoveEvent;
import com.pmi.tpd.api.event.publisher.IEventPublisher;
import com.pmi.tpd.api.scheduler.ILifecycleAwareSchedulerService;
import com.pmi.tpd.api.scheduler.SchedulerServiceException;
import com.pmi.tpd.core.maintenance.event.MaintenanceApplicationEvent;
import com.pmi.tpd.testing.junit5.MockitoTestCase;

public class DefaultMaintenanceModeHelperTest extends MockitoTestCase {

    @Mock
    private MaintenanceApplicationEvent event;

    @Mock
    private IEventPublisher eventPublisher;

    @InjectMocks
    private DefaultMaintenanceModeHelper helper;

    @Mock
    private ILifecycleAwareSchedulerService schedulerService;

    @Test
    public void testLock() throws Exception {
        try {
            helper.lock(event);
        } finally { // Done in a finally block to allow testLockSwallowsSchedulerExceptions to reuse it
            final InOrder ordered = inOrder(eventPublisher, schedulerService);
            ordered.verify(schedulerService).standby();
            ordered.verify(eventPublisher).publish(isA(AddEvent.class));
        }
    }

    @Test
    public void testLockSwallowsSchedulerExceptions() throws Exception {
        doThrow(SchedulerServiceException.class).when(schedulerService).standby();

        testLock();
    }

    @Test
    public void testUnlock() throws Exception {
        try {
            helper.unlock(event);
        } finally { // Done in a finally block to allow testUnlockSwallowsSchedulerExceptions to reuse it
            final InOrder ordered = inOrder(eventPublisher, schedulerService);
            ordered.verify(eventPublisher).publish(isA(RemoveEvent.class));
            ordered.verify(schedulerService).start();
        }
    }

    @Test
    public void testUnlockSwallowsSchedulerExceptions() throws Exception {
        doThrow(SchedulerServiceException.class).when(schedulerService).start();

        testUnlock();
    }
}
