package com.pmi.tpd.core.backup.task;

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

import com.pmi.tpd.api.event.publisher.IEventPublisher;
import com.pmi.tpd.api.exec.IRunnableTask;
import com.pmi.tpd.api.i18n.I18nService;
import com.pmi.tpd.api.i18n.KeyedMessage;
import com.pmi.tpd.api.i18n.support.SimpleI18nService;
import com.pmi.tpd.cluster.latch.ILatchableService;
import com.pmi.tpd.core.backup.BackupException;
import com.pmi.tpd.core.backup.CanceledBackupException;
import com.pmi.tpd.core.backup.IBackupState;
import com.pmi.tpd.core.backup.event.BackupCanceledEvent;
import com.pmi.tpd.core.backup.event.BackupFailedEvent;
import com.pmi.tpd.core.backup.event.BackupStartedEvent;
import com.pmi.tpd.core.backup.event.BackupSucceededEvent;
import com.pmi.tpd.core.event.advisor.EventAdvisorService;
import com.pmi.tpd.core.maintenance.MaintenanceModePhase;
import com.pmi.tpd.core.maintenance.event.MaintenanceApplicationEvent;
import com.pmi.tpd.database.spi.IDatabaseLatch;
import com.pmi.tpd.database.spi.IDatabaseManager;
import com.pmi.tpd.testing.junit5.MockitoTestCase;

public class BaseBackupTaskTest extends MockitoTestCase {

    @Mock
    private IDatabaseLatch databaseLatch;

    @Mock(lenient = true)
    private IDatabaseManager databaseManager;

    @Mock
    private IEventPublisher eventPublisher;

    @Spy
    private final I18nService i18nService = new SimpleI18nService(SimpleI18nService.Mode.RETURN_KEYS);

    @Mock
    private MaintenanceModePhase maintenanceModePhase;

    @Mock(answer = Answers.RETURNS_MOCKS, lenient = true)
    private IBackupTaskFactory taskFactory;

    private BaseBackupTask abstractBackupTask;

    @BeforeAll
    public static void setupEvents() throws IOException {
        EventAdvisorService.initialize(getResourceAsStream(BaseBackupTaskTest.class, "minimal-event-config.xml"));
    }

    @AfterAll
    public static void tearDownEvents() {
        EventAdvisorService.getInstance().terminate();
    }

    @BeforeEach
    public void setUp() {
        final BackupPhase backupPhase = mock(BackupPhase.class, withSettings().lenient());

        final BackupPhase.Builder backupPhaseBuilder = mock(BackupPhase.Builder.class, withSettings().lenient());
        doReturn(backupPhase).when(backupPhaseBuilder).build();
        doReturn(backupPhaseBuilder).when(backupPhaseBuilder).add(any(IRunnableTask.class), anyInt());

        when(databaseManager.getCurrentLatch()).thenReturn(databaseLatch);

        when(taskFactory.backupPhaseBuilder(any(IBackupState.class))).thenReturn(backupPhaseBuilder);

        abstractBackupTask = new BaseBackupTask(databaseManager, eventPublisher, i18nService, taskFactory,
                EventAdvisorService.getInstance(), new ILatchableService[] { databaseManager }) {

            @Override
            protected IRunnableTask createDelegateBackupTask(
                final BackupClientPlaceholderStep backupClientPlaceholderStep,
                final IBackupTaskFactory taskFactory,
                final IBackupState backupState,
                final MaintenanceApplicationEvent maintenanceEvent) {
                return maintenanceModePhase;
            }
        };
    }

    @Test
    public void testClientProgressCallbackAvailable() {
        // verify that a clientProgressCallback is provided
        assertNotNull(abstractBackupTask.getClientProgressCallback());
    }

    @Test
    public void testRunExitsInError() throws Exception {
        doThrow(new RuntimeException("faking an error from the nested task")).when(maintenanceModePhase).run();

        try {
            abstractBackupTask.run();
            fail("BackupTask.run should not have completed normally");
        } catch (final BackupException e) {
            // expected
            verify(eventPublisher).publish(isA(BackupStartedEvent.class));
            verify(databaseLatch).unlatch();
            verify(eventPublisher).publish(isA(BackupFailedEvent.class));

            assertFalse(e instanceof CanceledBackupException);
        }
    }

    @Test
    public void testRunIsCanceled() throws Exception {
        doThrow(new CanceledBackupException(mock(KeyedMessage.class))).when(maintenanceModePhase).run();

        try {
            abstractBackupTask.run();
            fail("BackupTask.run should not have completed normally");
        } catch (final CanceledBackupException e) {
            // expected
            verify(eventPublisher).publish(isA(BackupStartedEvent.class));
            verify(databaseLatch).unlatch();
            verify(eventPublisher).publish(isA(BackupCanceledEvent.class));
        }
    }

    @Test
    public void testShouldNotifyOfBackupStartAndSuccess() throws Exception {
        abstractBackupTask.run();
        verify(eventPublisher).publish(isA(BackupStartedEvent.class));
        verify(eventPublisher).publish(isA(BackupSucceededEvent.class));
    }
}
