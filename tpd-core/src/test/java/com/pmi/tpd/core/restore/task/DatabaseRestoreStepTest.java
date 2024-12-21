package com.pmi.tpd.core.restore.task;

import static com.pmi.tpd.core.backup.task.MaintenanceTaskTestHelper.assertProgress;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.concurrent.CountDownLatch;

import javax.sql.DataSource;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.Mockito;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import com.pmi.tpd.api.config.IApplicationConfiguration;
import com.pmi.tpd.api.i18n.I18nService;
import com.pmi.tpd.api.i18n.support.SimpleI18nService;
import com.pmi.tpd.api.lifecycle.ICancelState;
import com.pmi.tpd.core.backup.BackupException;
import com.pmi.tpd.core.backup.IBackupState;
import com.pmi.tpd.core.restore.IRestoreState;
import com.pmi.tpd.database.liquibase.backup.ILiquibaseAccessor;
import com.pmi.tpd.database.liquibase.backup.ILiquibaseBackupMonitor;
import com.pmi.tpd.database.liquibase.backup.ILiquibaseChangeSet;
import com.pmi.tpd.database.liquibase.backup.ILiquibaseMigrationDao;
import com.pmi.tpd.database.liquibase.backup.ILiquibaseRestoreMonitor;
import com.pmi.tpd.database.liquibase.backup.LiquibaseDataAccessException;
import com.pmi.tpd.database.spi.IDatabaseHandle;
import com.pmi.tpd.testing.junit5.MockitoTestCase;

public class DatabaseRestoreStepTest extends MockitoTestCase {

    public Path unzipDir;

    @Mock
    private IDatabaseHandle databaseHandle;

    @Mock
    private DataSource dataSource;

    @Mock
    private IRestoreState state;

    private final I18nService i18nService = new SimpleI18nService(SimpleI18nService.Mode.RETURN_KEYS);

    @Mock
    private IApplicationConfiguration settings;

    @Mock
    private ILiquibaseAccessor liquibaseDao;

    @BeforeEach
    public void setUp(@TempDir final Path path) throws IOException {
        this.unzipDir = path;
        when(state.getTargetDatabase()).thenReturn(databaseHandle);
        when(state.getUnzippedBackupDirectory()).thenReturn(unzipDir.toFile());
        when(databaseHandle.getDataSource()).thenReturn(dataSource);
        when(settings.getTemporaryDirectory()).thenReturn(unzipDir);

        // make sure the liquibase backup file actually exists
        Files.asCharSink(new File(unzipDir.toFile(), IBackupState.LIQUIBASE_BACKUP_FILE), Charsets.UTF_8).write("test");
    }

    @Test
    public void testCancel() throws Exception {
        final TestLiquibaseMigrationDao dao = new TestLiquibaseMigrationDao();
        final DatabaseRestoreStep step = new DatabaseRestoreStepTester(state, i18nService, settings, dao, liquibaseDao);

        final Thread thread = new Thread(step);
        thread.start();

        try {
            // wait for thread to start the step
            dao.awaitStart();

            // dao is running, cancelState has been passed to the dao
            assertFalse(dao.cancelState.isCanceled());
            step.cancel();
            // verify that the cancellation is visible from the dao
            assertTrue(dao.cancelState.isCanceled());
        } finally {
            dao.unlatch();
            thread.join();
        }
    }

    @Test
    public void testDaoThrowsDataAccessException() throws Exception {
        assertThrows(BackupException.class, () -> {
            final ILiquibaseMigrationDao dao = mock(ILiquibaseMigrationDao.class);
            doThrow(LiquibaseDataAccessException.class).when(dao)
                    .restore(Mockito.eq(liquibaseDao),
                        any(InputStream.class),
                        any(File.class),
                        any(ILiquibaseRestoreMonitor.class),
                        any(ICancelState.class));

            final DatabaseRestoreStep step = new DatabaseRestoreStepTester(state, i18nService, settings, dao,
                    liquibaseDao);

            // make sure the dao doesn't hold up processing
            step.run();
        });
    }

    @Test
    public void testProgress() throws InterruptedException {
        final TestLiquibaseMigrationDao dao = new TestLiquibaseMigrationDao();
        final DatabaseRestoreStep step = new DatabaseRestoreStepTester(state, i18nService, settings, dao, liquibaseDao);

        assertProgress("app.backup.restore.database", 0, step.getProgress());

        final Thread thread = new Thread(step);
        thread.start();

        try {
            // wait for thread to start the step
            dao.awaitStart();
            assertProgress("app.backup.restore.database", 0, step.getProgress());

            // provide feedback through the LiquibaseRestoreMonitor
            final ILiquibaseChangeSet changeSet1 = mockChangeSet(4, 30);
            final ILiquibaseChangeSet changeSet2 = mockChangeSet(2, 10);
            final ILiquibaseChangeSet changeSet3 = mockChangeSet(10, 60);

            dao.monitor.onBeginChangeset(changeSet1, 1, 3);
            assertProgress("app.restore.changeset.processing", 0, step.getProgress());

            dao.monitor.onAppliedChange();
            // processed 25% of cs1 weighted at 30%
            assertProgress("app.restore.changeset.processing", 30 / 4, step.getProgress());

            dao.monitor.onAppliedChange();
            dao.monitor.onAppliedChange();
            // processed 75% of cs1 weighted at 30%
            assertProgress("app.restore.changeset.processing", 3 * 30 / 4, step.getProgress());

            dao.monitor.onAppliedChange();
            dao.monitor.onFinishedChangeset();
            assertProgress("app.restore.changeset.processing", 30, step.getProgress());

            // changeset 2
            dao.monitor.onBeginChangeset(changeSet2, 2, 3);
            assertProgress("app.restore.changeset.processing", 30, step.getProgress());

            dao.monitor.onAppliedChange();
            assertProgress("app.restore.changeset.processing", 30 + 5, step.getProgress());
            dao.monitor.onAppliedChange();
            assertProgress("app.restore.changeset.processing", 30 + 10, step.getProgress());
            dao.monitor.onFinishedChangeset();

            // changeset 3
            dao.monitor.onBeginChangeset(changeSet3, 3, 3);
            for (int i = 1; i <= 10; ++i) {
                dao.monitor.onAppliedChange();
                assertProgress("incorrect progress for change " + i,
                    "app.restore.changeset.processing",
                    40 + 6 * i,
                    step.getProgress());
            }
            dao.monitor.onFinishedChangeset();

        } finally {
            // let the dao (and the thread) complete
            dao.unlatch();
            thread.join();
        }
    }

    @Test
    public void testRun() throws InterruptedException {
        final TestLiquibaseMigrationDao dao = new TestLiquibaseMigrationDao();
        final DatabaseRestoreStep step = new DatabaseRestoreStepTester(state, i18nService, settings, dao, liquibaseDao);

        final Thread thread = new Thread(step);
        thread.start();

        try {
            // wait for the dao to be called
            dao.awaitStart();
            // allow the DAO to complete
            dao.unlatch();

            // ensure the DAO has been called
            assertNotNull(dao.monitor);
            assertNotNull(dao.cancelState);
        } finally {
            thread.join();
        }
    }

    private static ILiquibaseChangeSet mockChangeSet(final long numChanges, final int weight) {
        final ILiquibaseChangeSet cs = mock(ILiquibaseChangeSet.class);
        when(cs.getChangeCount()).thenReturn(numChanges);
        when(cs.getWeight()).thenReturn(weight);

        return cs;
    }

    private static class DatabaseRestoreStepTester extends DatabaseRestoreStep {

        private final ILiquibaseAccessor liquibaseDao;

        private final ILiquibaseMigrationDao migrationDao;

        public DatabaseRestoreStepTester(final IRestoreState state, final I18nService i18nService,
                final IApplicationConfiguration settings, final ILiquibaseMigrationDao migrationDao,
                final ILiquibaseAccessor liquibaseDao) {
            super(state, i18nService, settings);
            this.migrationDao = migrationDao;
            this.liquibaseDao = liquibaseDao;
        }

        @Override
        protected ILiquibaseAccessor createLiquibaseAccessor(final DataSource dataSource) {
            return liquibaseDao;
        }

        @Override
        protected ILiquibaseMigrationDao createLiquibaseMigrationDao() {
            return migrationDao;
        }

    }

    private static class TestLiquibaseMigrationDao implements ILiquibaseMigrationDao {

        private final CountDownLatch completeLatch = new CountDownLatch(1);

        private final CountDownLatch runningLatch = new CountDownLatch(1);

        private ICancelState cancelState;

        private ILiquibaseRestoreMonitor monitor;

        public void awaitStart() throws InterruptedException {
            runningLatch.await();
        }

        @Override
        public void backup(final ILiquibaseAccessor liquibaseDao,
            final OutputStream stream,
            final String author,
            final ILiquibaseBackupMonitor monitor,
            final ICancelState cancelState) {
        }

        @Override
        public void restore(final ILiquibaseAccessor liquibaseDao,
            final InputStream stream,
            final File tempDir,
            final ILiquibaseRestoreMonitor monitor,
            final ICancelState cancelState) {
            this.cancelState = cancelState;
            this.monitor = monitor;

            runningLatch.countDown();
            try {
                completeLatch.await();
            } catch (final InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        public void unlatch() {
            completeLatch.countDown();
        }
    }
}
