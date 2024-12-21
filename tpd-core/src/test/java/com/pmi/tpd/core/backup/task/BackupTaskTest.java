package com.pmi.tpd.core.backup.task;

import static org.mockito.ArgumentMatchers.anyInt;

import java.io.IOException;
import java.util.List;

import com.pmi.tpd.api.event.publisher.IEventPublisher;
import com.pmi.tpd.api.exec.IRunnableTask;
import com.pmi.tpd.api.i18n.I18nService;
import com.pmi.tpd.api.i18n.support.SimpleI18nService;
import com.pmi.tpd.core.backup.IBackupState;
import com.pmi.tpd.core.event.advisor.EventAdvisorService;
import com.pmi.tpd.core.maintenance.LatchAndDrainDatabaseStep;
import com.pmi.tpd.core.maintenance.MaintenanceModePhase;
import com.pmi.tpd.core.maintenance.event.MaintenanceApplicationEvent;
import com.pmi.tpd.database.spi.IDatabaseLatch;
import com.pmi.tpd.database.spi.IDatabaseManager;
import com.pmi.tpd.testing.junit5.MockitoTestCase;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Spy;
import org.springframework.test.context.event.annotation.AfterTestClass;

public class BackupTaskTest extends MockitoTestCase {

    @Mock
    private BackupPhase backupPhase;

    @Mock
    private BackupPhase.Builder backupPhaseBuilder;

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

    @Mock
    private MaintenanceModePhase.Builder maintenanceModePhaseBuilder;

    @Mock(answer = Answers.RETURNS_MOCKS)
    private IBackupTaskFactory taskFactory;

    @BeforeAll
    public static void setupJohnson() throws IOException {
        EventAdvisorService.initialize(getResourceAsStream(BackupTaskTest.class, "minimal-event-config.xml"));
    }

    @AfterTestClass
    public static void tearDownJohnson() {
        EventAdvisorService.getInstance().terminate();
    }

    @BeforeEach
    public void setUp() {
        when(backupPhaseBuilder.build()).thenReturn(backupPhase);
        when(backupPhaseBuilder.add(any(IRunnableTask.class), anyInt())).thenReturn(backupPhaseBuilder);

        when(databaseManager.getCurrentLatch()).thenReturn(latch);

        when(maintenanceModePhaseBuilder.build()).thenReturn(maintenanceModePhase);
        when(maintenanceModePhaseBuilder.add(any(IRunnableTask.class), anyInt()))
                .thenReturn(maintenanceModePhaseBuilder);
        when(maintenanceModePhaseBuilder.event(any(MaintenanceApplicationEvent.class)))
                .thenReturn(maintenanceModePhaseBuilder);

        when(taskFactory.backupPhaseBuilder(any(IBackupState.class))).thenReturn(backupPhaseBuilder);
        when(taskFactory.maintenanceModePhaseBuilder()).thenReturn(maintenanceModePhaseBuilder);

        new BackupTask(databaseManager, eventPublisher, i18nService, taskFactory, EventAdvisorService.getInstance(),
                databaseManager);
    }

    @Test
    public void testConfig() {
        final ArgumentCaptor<IRunnableTask> taskCaptor = ArgumentCaptor.forClass(IRunnableTask.class);

        final ArgumentCaptor<Integer> weightCaptor = ArgumentCaptor.forClass(Integer.class);

        // Verify that the MaintenanceEvent was provided to the builder
        verify(maintenanceModePhaseBuilder).event(any(MaintenanceApplicationEvent.class));

        // Verify the _overall tasks_ on the root builder
        verify(maintenanceModePhaseBuilder, times(3)).add(taskCaptor.capture(), weightCaptor.capture());

        List<Integer> capturedWeights = weightCaptor.getAllValues();
        List<IRunnableTask> capturedTasks = taskCaptor.getAllValues();

        assertEquals(backupPhase, capturedTasks.get(0));
        assertEquals(50, capturedWeights.get(0).intValue());

        assertTrue(BackupClientPlaceholderStep.class.isAssignableFrom(capturedTasks.get(1).getClass()));
        assertEquals(50, capturedWeights.get(1).intValue());

        assertTrue(UnlatchDatabaseStep.class.isAssignableFrom(capturedTasks.get(2).getClass()));
        assertEquals(0, capturedWeights.get(2).intValue());

        verify(maintenanceModePhaseBuilder).build();

        final ArgumentCaptor<IRunnableTask> taskCaptor2 = ArgumentCaptor.forClass(IRunnableTask.class);

        final ArgumentCaptor<Integer> weightCaptor2 = ArgumentCaptor.forClass(Integer.class);

        verify(backupPhaseBuilder, times(4)).add(taskCaptor2.capture(), weightCaptor2.capture());

        capturedWeights = weightCaptor2.getAllValues();
        capturedTasks = taskCaptor2.getAllValues();

        assertTrue(ChangelogsBackupStep.class.isAssignableFrom(capturedTasks.get(0).getClass()));
        assertEquals(2, capturedWeights.get(0).intValue());

        assertTrue(LatchAndDrainDatabaseStep.class.isAssignableFrom(capturedTasks.get(1).getClass()));
        assertEquals(3, capturedWeights.get(1).intValue());

        assertTrue(DatabaseBackupStep.class.isAssignableFrom(capturedTasks.get(2).getClass()));
        assertEquals(93, capturedWeights.get(2).intValue());

        assertTrue(ConfigurationBackupStep.class.isAssignableFrom(capturedTasks.get(3).getClass()));
        assertEquals(2, capturedWeights.get(3).intValue());

        verify(backupPhaseBuilder).build();
    }
}
