package com.pmi.tpd.core.migration;

import static org.mockito.ArgumentMatchers.same;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;

import com.pmi.tpd.api.i18n.I18nService;
import com.pmi.tpd.api.i18n.support.SimpleI18nService;
import com.pmi.tpd.core.maintenance.IMaintenanceService;
import com.pmi.tpd.core.maintenance.ITaskMaintenanceMonitor;
import com.pmi.tpd.core.maintenance.MaintenanceType;
import com.pmi.tpd.core.migration.task.DatabaseMigrationTask;
import com.pmi.tpd.core.migration.task.DatabaseSetupTask;
import com.pmi.tpd.database.IDataSourceConfiguration;
import com.pmi.tpd.database.spi.IDatabaseManager;
import com.pmi.tpd.testing.junit5.MockitoTestCase;

public class DefaultMigrationServiceTest extends MockitoTestCase {

    @Mock
    private IDataSourceConfiguration configuration;

    @Spy
    private final I18nService i18nService = new SimpleI18nService();

    @Mock
    private IMaintenanceService maintenanceService;

    @Mock
    private IMigrationTaskFactory maintenanceTaskFactory;

    @Mock
    private IDatabaseManager databaseManager;

    @InjectMocks
    private DefaultMigrationService service;

    @Test
    public void testMigrate() {
        final DatabaseMigrationTask task = mock(DatabaseMigrationTask.class);
        final ITaskMaintenanceMonitor future = mock(ITaskMaintenanceMonitor.class);

        when(maintenanceTaskFactory.migrationTask(configuration)).thenReturn(task);
        doReturn(future).when(maintenanceService).start(same(task), eq(MaintenanceType.MIGRATION));

        assertSame(future, service.migrate(configuration));

        verify(maintenanceService).start(same(task), eq(MaintenanceType.MIGRATION));
        verify(maintenanceTaskFactory).migrationTask(configuration);
    }

    @Test
    public void testMigrateThrowsOnNullConfiguration() {
        assertThrows(NullPointerException.class, () -> {
            service.migrate(null);
        });
    }

    @Test
    public void testMigrateThrowsWhenMaintenanceInProgress() {
        assertThrows(MigrationException.class, () -> {
            final DatabaseMigrationTask task = mock(DatabaseMigrationTask.class);

            when(maintenanceTaskFactory.migrationTask(configuration)).thenReturn(task);
            doThrow(IllegalStateException.class).when(maintenanceService)
                    .start(same(task), eq(MaintenanceType.MIGRATION));

            try {
                service.migrate(configuration);
            } finally {
                verify(maintenanceService).start(same(task), eq(MaintenanceType.MIGRATION));
                verify(maintenanceTaskFactory).migrationTask(configuration);
            }

        });
    }

    @Test
    public void testSetup() {
        final DatabaseSetupTask task = mock(DatabaseSetupTask.class);
        final ITaskMaintenanceMonitor future = mock(ITaskMaintenanceMonitor.class);

        when(maintenanceTaskFactory.setupTask(configuration)).thenReturn(task);
        doReturn(future).when(maintenanceService).start(same(task), eq(MaintenanceType.MIGRATION));

        assertSame(future, service.setup(configuration));

        verify(maintenanceService).start(same(task), eq(MaintenanceType.MIGRATION));
        verify(maintenanceTaskFactory).setupTask(configuration);
    }

    @Test
    public void testSetupThrowsOnNullConfiguration() {
        assertThrows(NullPointerException.class, () -> {
            service.setup(null);
        });
    }

    @Test
    public void testSetupThrowsWhenMaintenanceInProgress() {
        assertThrows(MigrationException.class, () -> {
            final DatabaseSetupTask task = mock(DatabaseSetupTask.class);

            when(maintenanceTaskFactory.setupTask(configuration)).thenReturn(task);
            doThrow(IllegalStateException.class).when(maintenanceService)
                    .start(same(task), eq(MaintenanceType.MIGRATION));

            try {
                service.setup(configuration);
            } finally {
                verify(maintenanceService).start(same(task), eq(MaintenanceType.MIGRATION));
                verify(maintenanceTaskFactory).setupTask(configuration);
            }
        });
    }

    @Test
    public void testValidateConfiguration() {
        service.validateConfiguration(configuration);

        verify(databaseManager).validateConfiguration(same(configuration));
    }
}
