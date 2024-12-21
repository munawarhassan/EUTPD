package com.pmi.tpd.core.backup.task;

import static com.pmi.tpd.core.backup.task.MaintenanceTaskTestHelper.assertProgress;
import static org.mockito.ArgumentMatchers.same;

import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import com.pmi.tpd.api.i18n.I18nService;
import com.pmi.tpd.api.i18n.support.SimpleI18nService;
import com.pmi.tpd.database.spi.IDatabaseHandle;
import com.pmi.tpd.database.spi.IDatabaseLatch;
import com.pmi.tpd.database.spi.IDatabaseManager;
import com.pmi.tpd.testing.junit5.MockitoTestCase;

public class UnlatchDatabaseStepTest extends MockitoTestCase {

    private final I18nService i18nService = new SimpleI18nService(SimpleI18nService.Mode.RETURN_KEYS);

    @Mock
    private IDatabaseHandle databaseHandle;

    @Mock
    private IDatabaseLatch databaseLatch;

    @Mock
    private IDatabaseManager databaseManager;

    @Test
    public void testRunNotLatched() {
        assertThrows(IllegalStateException.class, () -> {
            builder().build().run();
        });
    }

    @Test
    public void testRunWithCurrentDatabase() {
        final UnlatchDatabaseStep step = builder().build();

        assertProgress("app.backup.restore.resuming.database", 0, step.getProgress());

        when(databaseManager.getCurrentLatch()).thenReturn(databaseLatch);
        step.run();

        assertProgress("app.backup.restore.resuming.database", 100, step.getProgress());
        verify(databaseLatch).unlatch();
    }

    @Test
    public void testRunWithTargetDatabase() {
        final UnlatchDatabaseStep step = builder().target(databaseHandle).build();

        assertProgress("app.backup.restore.switching.database", 0, step.getProgress());

        when(databaseManager.getCurrentLatch()).thenReturn(databaseLatch);
        step.run();

        assertProgress("app.backup.restore.switching.database", 100, step.getProgress());
        verify(databaseLatch).unlatchTo(same(databaseHandle));
    }

    private UnlatchDatabaseStep.Builder builder() {
        return new UnlatchDatabaseStep.Builder(i18nService, databaseManager);
    }
}
