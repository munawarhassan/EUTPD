package com.pmi.tpd.core.migration.task;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;

import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.Spy;

import com.pmi.tpd.api.event.advisor.IEventAdvisorService;
import com.pmi.tpd.api.event.publisher.IEventPublisher;
import com.pmi.tpd.api.exec.IRunnableTask;
import com.pmi.tpd.api.i18n.I18nService;
import com.pmi.tpd.api.i18n.support.SimpleI18nService;
import com.pmi.tpd.cluster.latch.LatchMode;
import com.pmi.tpd.core.backup.IBackupState;
import com.pmi.tpd.core.backup.task.BackupPhase;
import com.pmi.tpd.core.migration.CanceledMigrationException;
import com.pmi.tpd.core.migration.IMigrationTaskFactory;
import com.pmi.tpd.core.restore.IRestoreState;
import com.pmi.tpd.core.restore.RestorePhase;
import com.pmi.tpd.database.spi.IDatabaseHandle;
import com.pmi.tpd.database.spi.IDatabaseLatch;
import com.pmi.tpd.database.spi.IDatabaseManager;
import com.pmi.tpd.testing.junit5.MockitoTestCase;

public class BaseMigrationTaskTest extends MockitoTestCase {

    @Mock
    private BackupPhase backupPhase;

    @Mock
    private IDatabaseLatch databaseLatch;

    @Mock
    private IDatabaseManager databaseManager;

    @Mock
    private IDatabaseHandle sourceHandle;

    @Mock
    private IDatabaseHandle targetHandle;

    @Mock
    private IEventPublisher eventPublisher;

    @Mock
    private IEventAdvisorService<?> eventAdvisorService;

    @Spy
    private final I18nService i18nService = new SimpleI18nService(SimpleI18nService.Mode.RETURN_KEYS);

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private IMigrationTaskFactory maintenanceTaskFactory;

    @BeforeEach
    public void setUp() {
        final BackupPhase.Builder backupBuilder = mock(BackupPhase.Builder.class,
            withSettings().defaultAnswer(RETURNS_DEEP_STUBS));
        when(backupBuilder.add(any(IRunnableTask.class), anyInt())).thenReturn(backupBuilder);
        when(backupBuilder.build()).thenReturn(backupPhase);

        final RestorePhase.Builder restoreBuilder = mock(RestorePhase.Builder.class,
            withSettings().defaultAnswer(RETURNS_DEEP_STUBS));
        when(restoreBuilder.add(any(IRunnableTask.class), anyInt())).thenReturn(restoreBuilder);

        when(maintenanceTaskFactory.backupPhaseBuilder(any(IBackupState.class))).thenReturn(backupBuilder);
        when(maintenanceTaskFactory.restorePhaseBuilder(any(IRestoreState.class))).thenReturn(restoreBuilder);

        when(databaseManager.getHandle()).thenReturn(sourceHandle);
    }

    @Test
    public void testCancel() throws Exception {
        assertThrows(CanceledMigrationException.class, () -> {
            when(databaseManager.acquireLatch(LatchMode.LOCAL)).thenReturn(databaseLatch);

            final CyclicBarrier barrier = new CyclicBarrier(2);

            final BaseMigrationTask task = createTask();
            doAnswer(invocation -> {
                // release the other thread that's about to cancel task
                barrier.await();
                // wait for other thread to cancel
                barrier.await();
                return null;
            }).when(backupPhase).run();

            new Thread() {

                @Override
                public void run() {
                    try {
                        // wait until backupPhase is running
                        barrier.await();
                        task.cancel();
                        // release backupPhase
                        barrier.await();
                    } catch (final Exception e) {
                        fail("Interrupted while waiting");
                    }
                }
            }.start();

            task.run();
        });
    }

    @Test
    public void testRevertLatched() {
        final BaseMigrationTask task = createTask();
        when(databaseManager.getCurrentLatch()).thenReturn(databaseLatch);
        doThrow(UnsupportedOperationException.class).when(backupPhase).run();

        try {
            task.run();
        } catch (final UnsupportedOperationException e) {
            // expected
        }

        verify(databaseManager).getCurrentLatch();
        verify(databaseManager, never()).acquireLatch(LatchMode.LOCAL);
        verify(databaseLatch).unlatchTo(same(sourceHandle));
    }

    @Test
    public void testRevertNotLatched() {
        final BaseMigrationTask task = createTask();
        when(databaseManager.acquireLatch(LatchMode.LOCAL)).thenReturn(databaseLatch);
        doThrow(UnsupportedOperationException.class).when(backupPhase).run();

        try {
            task.run();
        } catch (final UnsupportedOperationException e) {
            // expected
        }

        verify(databaseManager).acquireLatch(LatchMode.LOCAL);
        verify(databaseLatch).drain(anyLong(), any(TimeUnit.class));
        verify(databaseLatch).forceDrain(anyLong(), any(TimeUnit.class));
        verify(databaseLatch).unlatchTo(same(sourceHandle));
    }

    protected BaseMigrationTask createTask() {
        final BaseMigrationTask task = new BaseMigrationTask(databaseManager, eventPublisher, maintenanceTaskFactory,
                i18nService, eventAdvisorService, targetHandle) {};
        task.setDrainTimeoutSeconds(0);
        task.setForceDrainTimeoutSeconds(0);
        return task;
    }

}
