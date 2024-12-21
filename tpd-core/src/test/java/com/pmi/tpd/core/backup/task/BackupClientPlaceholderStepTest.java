package com.pmi.tpd.core.backup.task;

import static com.pmi.tpd.core.backup.task.MaintenanceTaskTestHelper.assertProgress;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.pmi.tpd.api.i18n.I18nService;
import com.pmi.tpd.api.i18n.support.SimpleI18nService;
import com.pmi.tpd.testing.junit5.TestCase;

public class BackupClientPlaceholderStepTest extends TestCase {

    private final I18nService i18nService = new SimpleI18nService(SimpleI18nService.Mode.RETURN_KEYS);

    private BackupClientPlaceholderStep step;

    private Thread taskThread;

    private CountDownLatch stepFinished;

    @BeforeEach
    public void setUp() throws Exception {
        step = new BackupClientPlaceholderStep(i18nService);

        final CountDownLatch stepStarted = new CountDownLatch(1);
        stepFinished = new CountDownLatch(1);
        taskThread = new Thread(() -> {
            try {
                stepStarted.countDown();
                step.run();
            } finally {
                stepFinished.countDown();
            }
        });
        taskThread.start();
        stepStarted.await(1, TimeUnit.SECONDS);
    }

    @AfterEach
    public void tearDown() {
        // clean up the taskThread if it's running
        if (taskThread.isAlive()) {
            // try to stop the thread nicely
            step.cancel();
            try {
                taskThread.join(250);
            } catch (final InterruptedException e) {
                // ignore
            }

            if (taskThread.isAlive()) {
                // time to get rough
                taskThread.interrupt();
            }
        }
    }

    @Test
    public void testCancel() {
        step.cancel();

        // verify that the task completed
        assertStepFinished(true);
    }

    @Test
    public void testOnProgressUpdateNegative() {
        assertThrows(IllegalArgumentException.class, () -> step.onProgressUpdate(-1));
    }

    @Test
    public void testOnProgressUpdateGreaterThan100() {
        assertThrows(IllegalArgumentException.class, () -> step.onProgressUpdate(101));

    }

    @Test
    public void testOnProgressUpdate() {
        assertProgress("app.backup.home.dir", 0, step.getProgress());

        step.onProgressUpdate(6);
        assertProgress("app.backup.home.dir", 6, step.getProgress());
        assertStepFinished(false);

        step.onProgressUpdate(99);
        assertProgress("app.backup.home.dir", 99, step.getProgress());
        assertStepFinished(false);

        step.onProgressUpdate(99);
        assertProgress("app.backup.home.dir", 99, step.getProgress());
        assertStepFinished(false);

        step.onProgressUpdate(100);
        assertProgress("app.backup.home.dir", 100, step.getProgress());
        assertStepFinished(true);
    }

    private void assertStepFinished(final boolean finished) {
        try {
            assertEquals(finished, stepFinished.await(100, TimeUnit.MILLISECONDS));
        } catch (final InterruptedException e) {
            fail("Step hasn't finished - interrupted");
        }
    }
}
