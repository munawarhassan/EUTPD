package com.pmi.tpd.core.maintenance;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.pmi.tpd.api.exec.IProgress;

public class MaintenanceTaskTestHelper {

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
