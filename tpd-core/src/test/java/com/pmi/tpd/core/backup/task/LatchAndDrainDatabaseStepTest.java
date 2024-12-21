package com.pmi.tpd.core.backup.task;

import static com.pmi.tpd.core.backup.task.MaintenanceTaskTestHelper.assertProgress;
import static org.mockito.ArgumentMatchers.anyLong;

import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import com.pmi.tpd.api.i18n.I18nService;
import com.pmi.tpd.api.i18n.support.SimpleI18nService;
import com.pmi.tpd.cluster.latch.LatchMode;
import com.pmi.tpd.core.maintenance.LatchAndDrainDatabaseStep;
import com.pmi.tpd.core.migration.MigrationException;
import com.pmi.tpd.database.spi.IDatabaseLatch;
import com.pmi.tpd.database.spi.IDatabaseManager;
import com.pmi.tpd.testing.junit5.MockitoTestCase;

public class LatchAndDrainDatabaseStepTest extends MockitoTestCase {

    private final I18nService i18nService = new SimpleI18nService(SimpleI18nService.Mode.RETURN_KEYS);

    @Mock
    private IDatabaseManager databaseManager;

    @Mock
    private IDatabaseLatch latch;

    private LatchAndDrainDatabaseStep step;

    @BeforeEach
    public void setUp() throws Exception {
        when(databaseManager.acquireLatch(any(LatchMode.class))).thenReturn(latch);
        step = new TestLatchAndDrainDatabaseStep(i18nService, databaseManager, LatchMode.LOCAL);
        step.setDrainTimeoutSeconds(10);
        step.setForceDrainTimeoutSeconds(1);
    }

    @Test
    public void testDrainFailure() {
        assertThrows(MigrationException.class, () -> {
            assertProgress("app.migration.closingConnections", 0, step.getProgress());
            step.run();
        });
    }

    @Test
    public void testDrainSuccess() {
        assertProgress("app.migration.closingConnections", 0, step.getProgress());

        when(latch.drain(anyLong(), any(TimeUnit.class))).thenReturn(true);
        step.run();

        assertProgress("app.migration.closingConnections", 100, step.getProgress());
    }

    @Test
    public void testForceDrainSuccess() {
        assertProgress("app.migration.closingConnections", 0, step.getProgress());

        when(latch.drain(anyLong(), any(TimeUnit.class))).thenReturn(false);
        when(latch.forceDrain(anyLong(), any(TimeUnit.class))).thenReturn(true);
        step.run();

        assertProgress("app.migration.closingConnections", 100, step.getProgress());
    }

    @Test
    public void testForceDrainFailure() {
        assertThrows(MigrationException.class, () -> {
            assertProgress("app.migration.closingConnections", 0, step.getProgress());

            when(latch.drain(anyLong(), any(TimeUnit.class))).thenReturn(false);
            when(latch.forceDrain(anyLong(), any(TimeUnit.class))).thenReturn(false);
            step.run();
        });
    }

    private class TestLatchAndDrainDatabaseStep extends LatchAndDrainDatabaseStep {

        public TestLatchAndDrainDatabaseStep(final I18nService i18nService, final IDatabaseManager databaseManager,
                final LatchMode latchMode) {
            super(i18nService, databaseManager, latchMode);
        }
    }
}
