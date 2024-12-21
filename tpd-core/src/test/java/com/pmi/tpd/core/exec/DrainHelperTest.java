package com.pmi.tpd.core.exec;

import static com.pmi.tpd.scheduler.exec.support.DrainHelper.drain;
import static org.mockito.ArgumentMatchers.anyLong;

import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import com.pmi.tpd.cluster.latch.ILatch;
import com.pmi.tpd.testing.junit5.MockitoTestCase;

public class DrainHelperTest extends MockitoTestCase {

    @Mock
    private ILatch latch;

    @Test
    public void testDrainSuccess() throws Exception {
        when(latch.drain(anyLong(), eq(TimeUnit.SECONDS))).thenReturn(true);

        assertTrue(drain(latch, 0, 1), "Should have drained");

        verify(latch).drain(eq(0L), eq(TimeUnit.SECONDS));
        verify(latch, never()).forceDrain(anyLong(), eq(TimeUnit.SECONDS));
    }

    @Test
    public void testDrainFailureForceDrainSuccess() throws Exception {
        when(latch.drain(anyLong(), eq(TimeUnit.SECONDS))).thenReturn(false);
        when(latch.forceDrain(anyLong(), eq(TimeUnit.SECONDS))).thenReturn(true);

        assertTrue(drain(latch, 0, 1), "Should have drained");

        verify(latch).drain(eq(0L), eq(TimeUnit.SECONDS));
        verify(latch).forceDrain(eq(1L), eq(TimeUnit.SECONDS));
    }

    @Test
    public void testDrainFailureForceDrainFailure() throws Exception {
        when(latch.drain(anyLong(), eq(TimeUnit.SECONDS))).thenReturn(false);
        when(latch.forceDrain(anyLong(), eq(TimeUnit.SECONDS))).thenReturn(false);

        assertFalse(drain(latch, 0, 1), "Should not have drained");

        verify(latch).drain(eq(0L), eq(TimeUnit.SECONDS));
        verify(latch).forceDrain(eq(1L), eq(TimeUnit.SECONDS));
    }

    @Test
    public void testDrainFailureForceDrainAvoidance() throws Exception {
        when(latch.drain(anyLong(), eq(TimeUnit.SECONDS))).thenReturn(false);

        assertFalse(drain(latch, 0, -1), "Should not have drained");

        verify(latch).drain(eq(0L), eq(TimeUnit.SECONDS));
        verify(latch, never()).forceDrain(anyLong(), eq(TimeUnit.SECONDS));
    }
}
