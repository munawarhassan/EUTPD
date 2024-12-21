package com.pmi.tpd.cluster.concurrent;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import com.pmi.tpd.testing.junit5.MockitoTestCase;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

// partial test
public class StateTransferringExecutorServiceTest extends MockitoTestCase {

    @Mock
    private ExecutorService executor;

    private StateTransferringExecutorService executorService;

    @BeforeEach
    public void setUp() throws Exception {
        executorService = new StateTransferringExecutorService(executor);
    }

    @Test
    public void testStraightDelegations() throws Exception {
        executorService.awaitTermination(10L, TimeUnit.MILLISECONDS);
        verify(executor).awaitTermination(eq(10L), eq(TimeUnit.MILLISECONDS));
        executorService.isShutdown();
        verify(executor).isShutdown();
        executorService.isTerminated();
        verify(executor).isTerminated();
        executorService.shutdown();
        verify(executor).shutdown();
        executorService.shutdownNow();
        verify(executor).shutdownNow();
    }
}
