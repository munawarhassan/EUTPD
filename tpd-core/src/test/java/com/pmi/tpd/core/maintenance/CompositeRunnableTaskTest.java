package com.pmi.tpd.core.maintenance;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;

import org.junit.jupiter.api.Test;

import com.pmi.tpd.api.exec.IProgress;
import com.pmi.tpd.api.exec.IRunnableTask;
import com.pmi.tpd.scheduler.exec.CompositeRunableTask;
import com.pmi.tpd.testing.junit5.TestCase;

public class CompositeRunnableTaskTest extends TestCase {

    @Test
    public void testCancel() {
        final SmallStep step1 = new SmallStep("step 1");
        final SmallStep step2 = new SmallStep("step 2");

        final CompositeRunableTask giantLeap = new CompositeRunableTask.Builder().add(step1, 10).add(step2, 30).build();

        assertFalse(step1.canceled);
        assertFalse(step2.canceled);

        giantLeap.cancel();

        // verify that all steps are cancelled, even if they're not running yet
        assertTrue(step1.canceled);
        assertTrue(step2.canceled);
    }

    @Test
    public void testEmpty() throws Exception {
        assertThrows(IllegalStateException.class, () -> {
            new CompositeRunableTask.Builder().build();
        });
    }

    @Test
    public void testProgress() throws Exception {
        final SmallStep step1 = new SmallStep("step 1");
        final SmallStep step2 = new SmallStep("step 2");
        final SmallStep step3 = new SmallStep("step 3");

        final CompositeRunableTask giantLeap = CompositeRunableTask.builder()
                .add(step1, 20) // total weight
                                // adds up to
                                // 200
                .add(step2, 60)
                .add(step3, 120)
                .build();

        // start a second thread that runs the composite task
        final CountDownLatch started = new CountDownLatch(1);
        final Thread thread = new Thread(() -> {
            started.countDown();
            giantLeap.run();
        });
        thread.start();

        // give the second thread time to start
        started.await(1, TimeUnit.SECONDS);

        MaintenanceTaskTestHelper.assertProgress("step 1", 0, giantLeap.getProgress());

        // let step1 progress 20% - overall progress should be 10% * 20% = 2%
        step1.progress = 20;
        MaintenanceTaskTestHelper.assertProgress("step 1", 2, giantLeap.getProgress());
        step1.progress = 90;
        MaintenanceTaskTestHelper.assertProgress("step 1", 9, giantLeap.getProgress());
        step1.progress = 96; // check rounding
        MaintenanceTaskTestHelper.assertProgress("step 1", 10, giantLeap.getProgress());
        step1.complete();

        assertTrue(step2.awaitStart(1, TimeUnit.SECONDS));
        MaintenanceTaskTestHelper.assertProgress("step 2", 10, giantLeap.getProgress());
        step2.progress = 50;
        MaintenanceTaskTestHelper.assertProgress("step 2", 25, giantLeap.getProgress());
        step2.progress = 150; // test going over the limits
        MaintenanceTaskTestHelper.assertProgress("step 2", 40, giantLeap.getProgress());

        step3.progress = 20; // test that step3 progress is already taken into account, it might be running
                             // in parallel
        MaintenanceTaskTestHelper.assertProgress("step 2", 52, giantLeap.getProgress());

        step2.complete();

        assertTrue(step3.awaitStart(1, TimeUnit.SECONDS));
        MaintenanceTaskTestHelper.assertProgress("step 3", 52, giantLeap.getProgress());
        step3.progress = 100;
        step3.complete();

        // wait for giantLeap to complete
        thread.join();

        MaintenanceTaskTestHelper.assertProgress("step 3", 100, giantLeap.getProgress());
    }

    @Test
    public void testComplexProgress() throws Exception {
        final SmallStep step1 = new SmallStep("step 1");
        final SmallStep step2 = new SmallStep("step 2");
        final SmallStep step3 = new SmallStep("step 3");
        final SmallStep step4 = new SmallStep("step 4");
        final SmallStep step5 = new SmallStep("step 5");
        final SmallStep step6 = new SmallStep("step 6");
        final SmallStep step7 = new SmallStep("step 7");
        final SmallStep step8 = new SmallStep("step 8");

        final CompositeRunableTask giantLeap = CompositeRunableTask.builder()
                .add(CompositeRunableTask.builder()
                        .add(step1, 5) // 5
                        .add(step2, 2) // 7
                        .add(step3, 90) // 97
                        .add(step4, 3) // 100
                        .build(),
                    82)
                .add(CompositeRunableTask.builder()
                        .add(step5, 0) // 0
                        .add(step6, 100) // 100
                        .add(step7, 0) // 100
                        .build(),
                    18) // 100
                        // overall
                .add(step8, 0)
                .build();

        // start a second thread that runs the composite task
        final CountDownLatch started = new CountDownLatch(1);
        final Thread thread = new Thread(() -> {
            started.countDown();
            giantLeap.run();
        });
        thread.start();

        // give the second thread time to start
        started.await(1, TimeUnit.SECONDS);

        MaintenanceTaskTestHelper.assertProgress("step 1", 0, giantLeap.getProgress());

        step1.progress = 5;
        MaintenanceTaskTestHelper.assertProgress("step 1", 1, giantLeap.getProgress());
        step1.progress = 90;
        MaintenanceTaskTestHelper.assertProgress("step 1", 5, giantLeap.getProgress());
        step1.progress = 96; // check rounding
        MaintenanceTaskTestHelper.assertProgress("step 1", 5, giantLeap.getProgress());
        step1.complete();

        assertTrue(step2.awaitStart(1, TimeUnit.SECONDS));
        MaintenanceTaskTestHelper.assertProgress("step 2", 5, giantLeap.getProgress());
        step2.progress = 50;
        MaintenanceTaskTestHelper.assertProgress("step 2", 5, giantLeap.getProgress());
        step2.progress = 150; // test going over the limits
        MaintenanceTaskTestHelper.assertProgress("step 2", 6, giantLeap.getProgress());
        step2.complete();

        assertTrue(step3.awaitStart(1, TimeUnit.SECONDS));
        MaintenanceTaskTestHelper.assertProgress("step 3", 6, giantLeap.getProgress());
        step3.complete();

        assertTrue(step4.awaitStart(1, TimeUnit.SECONDS));
        step4.progress = 10;
        MaintenanceTaskTestHelper.assertProgress("step 4", 81, giantLeap.getProgress());
        step4.progress = 90;
        MaintenanceTaskTestHelper.assertProgress("step 4", 82, giantLeap.getProgress());
        step4.complete();

        assertTrue(step5.awaitStart(1, TimeUnit.SECONDS));
        step5.progress = 10;
        MaintenanceTaskTestHelper.assertProgress("step 5", 82, giantLeap.getProgress());
        step5.progress = 90;
        MaintenanceTaskTestHelper.assertProgress("step 5", 82, giantLeap.getProgress());
        step5.complete();

        assertTrue(step6.awaitStart(1, TimeUnit.SECONDS));
        step6.progress = 10;
        MaintenanceTaskTestHelper.assertProgress("step 6", 84, giantLeap.getProgress());
        step6.progress = 90;
        MaintenanceTaskTestHelper.assertProgress("step 6", 99, giantLeap.getProgress());
        step6.complete();

        assertTrue(step7.awaitStart(1, TimeUnit.SECONDS));
        step7.progress = 10;
        MaintenanceTaskTestHelper.assertProgress("step 7", 100, giantLeap.getProgress());
        step7.progress = 90;
        MaintenanceTaskTestHelper.assertProgress("step 7", 100, giantLeap.getProgress());
        step7.complete();

        assertTrue(step8.awaitStart(1, TimeUnit.SECONDS));
        step8.progress = 10;
        MaintenanceTaskTestHelper.assertProgress("step 8", 100, giantLeap.getProgress());
        step8.progress = 90;
        MaintenanceTaskTestHelper.assertProgress("step 8", 100, giantLeap.getProgress());
        step8.progress = 100;
        step8.complete();

        // wait for giantLeap to complete
        thread.join();

        MaintenanceTaskTestHelper.assertProgress("step 8", 100, giantLeap.getProgress());
    }

    private static class SmallStep implements IRunnableTask, IProgress {

        private final CountDownLatch start = new CountDownLatch(1);

        private final CountDownLatch stop = new CountDownLatch(1);

        volatile int progress = 0;

        volatile boolean canceled;

        private final String message;

        private SmallStep(final String message) {
            this.message = message;
        }

        public boolean awaitStart(final long timeout, final TimeUnit timeUnit) throws InterruptedException {
            return start.await(timeout, timeUnit);
        }

        @Override
        public String getName() {
            return message;
        }

        @Override
        public void cancel() {
            canceled = true;
        }

        public void complete() {
            stop.countDown();
        }

        @Nonnull
        @Override
        public IProgress getProgress() {
            return this;
        }

        @Nonnull
        @Override
        public String getMessage() {
            return message;
        }

        @Override
        public int getPercentage() {
            return progress;
        }

        @Override
        public void run() {
            try {
                start.countDown();
                stop.await();
            } catch (final InterruptedException e) {
                // ignore
            }
        }
    }
}
