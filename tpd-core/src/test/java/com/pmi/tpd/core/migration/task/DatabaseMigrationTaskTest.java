package com.pmi.tpd.core.migration.task;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.isA;

import java.io.IOException;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.Spy;

import com.pmi.tpd.api.event.advisor.IEventAdvisorService;
import com.pmi.tpd.api.event.publisher.IEventPublisher;
import com.pmi.tpd.api.exec.IRunnableTask;
import com.pmi.tpd.api.i18n.I18nService;
import com.pmi.tpd.api.i18n.KeyedMessage;
import com.pmi.tpd.api.i18n.support.SimpleI18nService;
import com.pmi.tpd.core.backup.IBackupState;
import com.pmi.tpd.core.backup.task.BackupPhase;
import com.pmi.tpd.core.event.advisor.EventAdvisorService;
import com.pmi.tpd.core.maintenance.MaintenanceModePhase;
import com.pmi.tpd.core.maintenance.event.MaintenanceApplicationEvent;
import com.pmi.tpd.core.migration.CanceledMigrationException;
import com.pmi.tpd.core.migration.IMigrationTaskFactory;
import com.pmi.tpd.core.migration.MigrationException;
import com.pmi.tpd.core.migration.event.MigrationCanceledEvent;
import com.pmi.tpd.core.migration.event.MigrationFailedEvent;
import com.pmi.tpd.core.migration.event.MigrationStartedEvent;
import com.pmi.tpd.core.migration.event.MigrationSucceededEvent;
import com.pmi.tpd.core.restore.IRestoreState;
import com.pmi.tpd.core.restore.RestorePhase;
import com.pmi.tpd.database.spi.IDatabaseHandle;
import com.pmi.tpd.database.spi.IDatabaseLatch;
import com.pmi.tpd.database.spi.IDatabaseManager;
import com.pmi.tpd.testing.junit5.MockitoTestCase;

public class DatabaseMigrationTaskTest extends MockitoTestCase {

    @Mock
    private IDatabaseHandle databaseHandle;

    @Mock(lenient = true)
    private IDatabaseManager databaseManager;

    @Mock
    private IEventPublisher eventPublisher;

    @Spy
    private final I18nService i18nService = new SimpleI18nService(SimpleI18nService.Mode.RETURN_KEYS);

    @Mock
    private IDatabaseLatch latch;

    @Mock
    private MaintenanceModePhase maintenanceModePhase;

    private DatabaseMigrationTask task;

    @Mock(answer = Answers.RETURNS_MOCKS)
    private IMigrationTaskFactory taskFactory;

    private static IEventAdvisorService<?> eventAdvisorService;

    @BeforeAll
    public static void setupJohnson() throws IOException {
        eventAdvisorService = EventAdvisorService
                .initialize(getResourceAsStream(DatabaseMigrationTaskTest.class, "minimal-event-config.xml"));
    }

    @AfterAll
    public static void tearDownJohnson() {
        eventAdvisorService.terminate();
    }

    @BeforeEach
    public void setUp() {
        final MaintenanceModePhase.Builder maintenanceModePhaseBuilder = mock(MaintenanceModePhase.Builder.class);
        when(taskFactory.maintenanceModePhaseBuilder()).thenReturn(maintenanceModePhaseBuilder);
        when(maintenanceModePhaseBuilder.build()).thenReturn(maintenanceModePhase);
        when(maintenanceModePhaseBuilder.add(any(IRunnableTask.class), anyInt()))
                .thenReturn(maintenanceModePhaseBuilder);
        when(maintenanceModePhaseBuilder.event(any(MaintenanceApplicationEvent.class)))
                .thenReturn(maintenanceModePhaseBuilder);

        final BackupPhase backupPhase = mock(BackupPhase.class);
        final BackupPhase.Builder backupPhaseBuilder = mock(BackupPhase.Builder.class);
        when(taskFactory.backupPhaseBuilder(any(IBackupState.class))).thenReturn(backupPhaseBuilder);
        when(backupPhaseBuilder.build()).thenReturn(backupPhase);
        when(backupPhaseBuilder.add(any(IRunnableTask.class), anyInt())).thenReturn(backupPhaseBuilder);

        final RestorePhase restorePhase = mock(RestorePhase.class);
        final RestorePhase.Builder restorePhaseBuilder = mock(RestorePhase.Builder.class);
        when(taskFactory.restorePhaseBuilder(any(IRestoreState.class))).thenReturn(restorePhaseBuilder);
        when(restorePhaseBuilder.build()).thenReturn(restorePhase);
        when(restorePhaseBuilder.add(any(IRunnableTask.class), anyInt())).thenReturn(restorePhaseBuilder);

        when(databaseManager.getCurrentLatch()).thenReturn(latch);

        task = new DatabaseMigrationTask(databaseManager, eventPublisher, taskFactory, i18nService, eventAdvisorService,
                databaseHandle);
        task.setDrainTimeoutSeconds(0);
        task.setForceDrainTimeoutSeconds(0);
    }

    @Test
    public void testMigrationExitsInError() throws Exception {
        doThrow(new RuntimeException("faking an error from the nested task")).when(maintenanceModePhase).run();

        try {
            task.run();
            fail("DatabaseMigrationTask.run should not have completed normally");
        } catch (final MigrationException e) {
            // expected
            verify(eventPublisher).publish(isA(MigrationStartedEvent.class));
            verify(eventPublisher).publish(isA(MigrationFailedEvent.class));

            assertThat(e, not(instanceOf(CanceledMigrationException.class)));
            assertThat(e.getMessageKey(), equalTo("app.migration.failed"));
        }
    }

    @Test
    public void testMigrationIsCanceled() throws Exception {
        doThrow(new CanceledMigrationException(mock(KeyedMessage.class))).when(maintenanceModePhase).run();

        try {
            task.run();
            fail("DatabaseMigrationTask.run should not have completed normally");
        } catch (final CanceledMigrationException e) {
            // expected
            verify(eventPublisher).publish(isA(MigrationStartedEvent.class));
            verify(eventPublisher).publish(isA(MigrationCanceledEvent.class));

            assertThat(e, instanceOf(CanceledMigrationException.class));
        }
    }

    @Test
    public void testShouldNotifyOfMigrationStartAndSuccess() throws Exception {
        task.run();
        verify(eventPublisher).publish(isA(MigrationStartedEvent.class));
        verify(eventPublisher).publish(isA(MigrationSucceededEvent.class));
    }
}
