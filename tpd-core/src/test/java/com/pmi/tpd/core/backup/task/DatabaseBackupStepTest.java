package com.pmi.tpd.core.backup.task;

import static com.pmi.tpd.core.backup.task.MaintenanceTaskTestHelper.assertProgress;
import static org.mockito.ArgumentMatchers.anyString;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Optional;

import javax.sql.DataSource;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;

import com.pmi.tpd.api.i18n.I18nService;
import com.pmi.tpd.api.i18n.support.SimpleI18nService;
import com.pmi.tpd.api.lifecycle.ICancelState;
import com.pmi.tpd.api.user.IUser;
import com.pmi.tpd.core.backup.BackupException;
import com.pmi.tpd.core.backup.IBackupState;
import com.pmi.tpd.database.liquibase.backup.ILiquibaseAccessor;
import com.pmi.tpd.database.liquibase.backup.ILiquibaseBackupMonitor;
import com.pmi.tpd.database.liquibase.backup.ILiquibaseMigrationDao;
import com.pmi.tpd.database.liquibase.backup.LiquibaseDataAccessException;
import com.pmi.tpd.database.spi.IDatabaseHandle;
import com.pmi.tpd.security.IAuthenticationContext;
import com.pmi.tpd.testing.junit5.MockitoTestCase;

import de.schlichtherle.truezip.zip.ZipEntry;
import de.schlichtherle.truezip.zip.ZipOutputStream;

public class DatabaseBackupStepTest extends MockitoTestCase {

    @Mock(lenient = true)
    private IAuthenticationContext authenticationContext;

    @Mock
    private DataSource backupDataSource;

    @Mock(lenient = true)
    private IBackupState backupState;

    @Spy
    private final I18nService i18nService = new SimpleI18nService(SimpleI18nService.Mode.RETURN_KEYS);

    @Mock
    private ILiquibaseMigrationDao migrationDao;

    @Mock
    private ILiquibaseAccessor liquibaseDao;

    @InjectMocks
    private DatabaseBackupStepTester step;

    @Mock
    private ZipOutputStream zipStream;

    private static class DatabaseBackupStepTester extends DatabaseBackupStep {

        private final ILiquibaseMigrationDao migrationDao;

        private final ILiquibaseAccessor liquibaseDao;

        public DatabaseBackupStepTester(final IBackupState state, final IAuthenticationContext authenticationContext,
                final I18nService i18nService, final ILiquibaseMigrationDao migrationDao,
                final ILiquibaseAccessor liquibaseDao) {
            super(state, authenticationContext, i18nService);
            this.liquibaseDao = liquibaseDao;
            this.migrationDao = migrationDao;
        }

        @Override
        protected ILiquibaseAccessor createLiquibaseAccessor(final DataSource datasource) {
            return liquibaseDao;
        }

        @Override
        protected ILiquibaseMigrationDao createLiquibaseMigrationDao() {
            return migrationDao;
        }

    }

    @BeforeEach
    public void setUp() {
        when(backupState.getBackupZipStream()).thenReturn(zipStream);
        final IDatabaseHandle handle = mock(IDatabaseHandle.class, withSettings().lenient());
        when(handle.getDataSource()).thenReturn(backupDataSource);
        when(backupState.getSourceDatabase()).thenReturn(handle);
    }

    @Test
    public void testCancel() {
        step.run();

        // verify that the CancelState that is passed down to the migrationDao responds to step.cancel
        final ArgumentCaptor<ICancelState> captor = ArgumentCaptor.forClass(ICancelState.class);
        verify(migrationDao).backup(eq(
            liquibaseDao), any(OutputStream.class), anyString(), any(ILiquibaseBackupMonitor.class), captor.capture());

        final ICancelState cancelState = captor.getValue();
        assertFalse(cancelState.isCanceled());

        step.cancel();
        assertTrue(cancelState.isCanceled());
    }

    @Test
    public void testProgress() {
        // when the task hasn't started, the progress should still be sensible
        assertProgress("app.backup.backup.liquibase", 0, step.getProgress());

        // simulate a LiquibaseBackupMonitor callback
        step.started(10);
        assertProgress("app.backup.backup.liquibase", 0, step.getProgress());

        // simulate a single row
        step.rowWritten();
        assertProgress("app.backup.backup.liquibase", 10, step.getProgress());

        // simulate 100% complete
        for (int i = 2; i <= 10; ++i) {
            step.rowWritten();
            assertProgress("app.backup.backup.liquibase", 10 * i, step.getProgress());
        }

        // simulate one too many callbacks
        step.rowWritten();
        assertProgress("app.backup.backup.liquibase", 100, step.getProgress());
    }

    @Test
    public void testRunAuthenticated() throws Exception {
        final IUser user = mock(IUser.class);
        when(user.getUsername()).thenReturn("user");
        when(authenticationContext.isAuthenticated()).thenReturn(true);
        when(authenticationContext.getCurrentUser()).thenReturn(Optional.of(user));

        step.run();

        inOrder(zipStream, zipStream);

        verify(zipStream).putNextEntry(any(ZipEntry.class));
        verify(migrationDao).backup(eq(liquibaseDao),
            any(OutputStream.class),
            eq("user"),
            any(ILiquibaseBackupMonitor.class),
            any(ICancelState.class));
        verify(zipStream).closeEntry();
    }

    @Test
    public void testRunIoException() throws Exception {
        assertThrows(BackupException.class, () -> {
            doThrow(new IOException("Zip closed")).when(zipStream).putNextEntry(any(ZipEntry.class));
            step.run();
        });
    }

    @Test
    public void testRunLiquibaseException() throws Exception {
        assertThrows(BackupException.class, () -> {
            doThrow(new LiquibaseDataAccessException("", null)).when(migrationDao)
                    .backup(eq(liquibaseDao),
                        any(OutputStream.class),
                        anyString(),
                        any(ILiquibaseBackupMonitor.class),
                        any(ICancelState.class));

            step.run();
        });
    }

    @Test
    public void testRunUnauthenticated() throws Exception {
        step.run();

        inOrder(zipStream, zipStream);

        verify(zipStream).putNextEntry(any(ZipEntry.class));
        verify(migrationDao).backup(eq(liquibaseDao),
            any(OutputStream.class),
            eq(DatabaseBackupStep.DEFAULT_AUTHOR),
            any(ILiquibaseBackupMonitor.class),
            any(ICancelState.class));
        verify(zipStream).closeEntry();
    }
}
