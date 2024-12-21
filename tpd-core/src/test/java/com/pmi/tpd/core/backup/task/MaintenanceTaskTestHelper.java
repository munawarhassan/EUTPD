package com.pmi.tpd.core.backup.task;

import com.pmi.tpd.api.exec.IProgress;
import com.pmi.tpd.testing.junit5.TestCase;

public class MaintenanceTaskTestHelper extends TestCase {

    public static void assertProgress(final String assertMessage,
        final String message,
        final int percentage,
        final IProgress actualProgress) {
        assertNotNull(actualProgress, assertMessage);
        assertEquals(message, actualProgress.getMessage(), assertMessage);
        assertEquals(percentage, actualProgress.getPercentage(), assertMessage);
    }

    public static void assertProgress(final String message, final int percentage, final IProgress actualProgress) {
        assertProgress(null, message, percentage, actualProgress);
    }
}
