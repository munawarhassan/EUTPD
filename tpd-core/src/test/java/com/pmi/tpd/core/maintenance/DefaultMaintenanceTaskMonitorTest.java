package com.pmi.tpd.core.maintenance;

import static org.mockito.ArgumentMatchers.same;

import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;

import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import com.pmi.tpd.api.exec.ICompletionCallback;
import com.pmi.tpd.api.exec.IProgress;
import com.pmi.tpd.api.exec.IRunnableTask;
import com.pmi.tpd.api.exec.ProgressTask;
import com.pmi.tpd.api.exec.TaskState;
import com.pmi.tpd.api.i18n.I18nService;
import com.pmi.tpd.api.i18n.KeyedMessage;
import com.pmi.tpd.api.i18n.support.SimpleI18nService;
import com.pmi.tpd.core.migration.CanceledMigrationException;
import com.pmi.tpd.scheduler.exec.IncorrectTokenException;
import com.pmi.tpd.testing.junit5.MockitoTestCase;

public class DefaultMaintenanceTaskMonitorTest extends MockitoTestCase {

    private static final String CANCEL_TOKEN = "cancel";

    private static final String ID = "id";

    private static final UUID NODE_ID = UUID.randomUUID();

    private static final String SESSION_ID = "sessionId";

    @Mock
    private IRunnableTask task;

    @Mock
    private ICompletionCallback callback;

    private final I18nService i18nService = new SimpleI18nService(SimpleI18nService.Mode.RETURN_KEYS);

    @Test
    public void testCallbackCancel() throws Exception {
        final CountDownLatch running = new CountDownLatch(1);
        final IRunnableTask task = new CancelableTask(running);
        final DefaultMaintenanceTaskMonitor monitor = create(task);
        monitor.registerCallback(callback);

        final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
        monitor.submitTo(executorService);

        // wait for the task to start
        running.await(1, TimeUnit.SECONDS);

        // cancel should succeed and return true
        assertTrue(monitor.cancel(CANCEL_TOKEN, 200, TimeUnit.MILLISECONDS));
        verify(callback).onCancellation();
    }

    @Test
    public void testCallbackCancelBeforeTaskStarts() throws Exception {
        final CountDownLatch running = new CountDownLatch(1);
        final IRunnableTask task = new CancelableTask(running);
        final DefaultMaintenanceTaskMonitor monitor = create(task);
        monitor.registerCallback(callback);

        final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
        monitor.submitTo(executorService);

        assertTrue(monitor.cancel(CANCEL_TOKEN, 200, TimeUnit.MILLISECONDS));
        verify(callback).onCancellation();
    }

    @Test
    public void testCallbackFailure() {
        final DefaultMaintenanceTaskMonitor monitor = create(task);
        final Exception exception = new RuntimeException("oh noooos");
        doThrow(exception).when(task).run();
        monitor.registerCallback(callback);
        try {
            monitor.run();
        } catch (final RuntimeException e) {
            // expected
        }

        verify(callback).onFailure(same(exception));
    }

    @Test
    public void testCallbackSuccess() {
        final DefaultMaintenanceTaskMonitor monitor = create(task);
        monitor.registerCallback(callback);
        monitor.run();

        verify(callback).onSuccess();
    }

    @Test
    public void testCancelAfterTaskFinished() {
        final DefaultMaintenanceTaskMonitor monitor = create(task);
        monitor.run();

        assertFalse(monitor.cancel(CANCEL_TOKEN, 20, TimeUnit.MILLISECONDS));
        assertEquals(TaskState.SUCCESSFUL, monitor.getState());
    }

    @Test
    public void testCancelBeforeTaskStarts() {
        final DefaultMaintenanceTaskMonitor monitor = create(task);

        assertFalse(monitor.cancel(CANCEL_TOKEN, 20, TimeUnit.MILLISECONDS));
        assertEquals(TaskState.RUNNING, monitor.getState());
    }

    @Test
    public void testCancelIncorrectToken() {
        assertThrows(IncorrectTokenException.class, () -> {
            final DefaultMaintenanceTaskMonitor monitor = create(task);

            monitor.cancel("wrong", 1, TimeUnit.SECONDS);
        });
    }

    @Test
    public void testCancelWhileTaskRuns() throws Exception {
        final CountDownLatch running = new CountDownLatch(1);
        final IRunnableTask task = new CancelableTask(running);
        final DefaultMaintenanceTaskMonitor monitor = create(task);

        new Thread(monitor).start();

        // wait for the task to start
        running.await(1, TimeUnit.SECONDS);

        // cancel should succeed and return true
        assertTrue(monitor.cancel(CANCEL_TOKEN, 200, TimeUnit.MILLISECONDS));
        assertEquals(TaskState.CANCELED, monitor.getState());
    }

    private DefaultMaintenanceTaskMonitor create(final IRunnableTask task) {
        return new DefaultMaintenanceTaskMonitor(task, ID, MaintenanceType.BACKUP, NODE_ID, SESSION_ID, CANCEL_TOKEN,
                i18nService);
    }

    private static class CancelableTask implements IRunnableTask {

        private final CountDownLatch cancelLatch = new CountDownLatch(1);

        private final CountDownLatch runningLatch;

        private CancelableTask(final CountDownLatch runningLatch) {
            this.runningLatch = runningLatch;
        }

        @Override
        public String getName() {
            return this.getClass().getSimpleName();
        }

        @Override
        public void cancel() {
            cancelLatch.countDown();
        }

        @Nonnull
        @Override
        public IProgress getProgress() {
            return new ProgressTask("progress", 0);
        }

        @Override
        public void run() {
            // signal that the task is now running
            runningLatch.countDown();
            try {
                cancelLatch.await(1, TimeUnit.HOURS);
                throw new CanceledMigrationException(mock(KeyedMessage.class));
            } catch (final InterruptedException e) {
                fail("Interrupted while waiting for cancel");
            }
        }
    }
}
