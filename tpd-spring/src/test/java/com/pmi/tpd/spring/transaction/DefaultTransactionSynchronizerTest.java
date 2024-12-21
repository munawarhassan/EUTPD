package com.pmi.tpd.spring.transaction;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.pmi.tpd.testing.junit5.MockitoTestCase;

public class DefaultTransactionSynchronizerTest extends MockitoTestCase {

    private final DefaultTransactionSynchronizer synchronizer = new DefaultTransactionSynchronizer();

    @Mock
    private TransactionSynchronization synchronization;

    @AfterEach
    public void tearDown() throws Exception {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.clear();
        } else {
            TransactionSynchronizationManager.setActualTransactionActive(false);
        }
    }

    @Test
    public void testIsAvailable() throws Exception {
        assertFalse(synchronizer.isAvailable());

        // Without an actual transaction active, just having synchronization in place is not enough
        TransactionSynchronizationManager.initSynchronization();
        assertFalse(synchronizer.isAvailable());

        // With both a transaction active _and_ synchronization in place, the synchronizer should be available
        TransactionSynchronizationManager.setActualTransactionActive(true);
        assertTrue(synchronizer.isAvailable());
    }

    @Test
    public void testRegister() throws Exception {
        TransactionSynchronizationManager.initSynchronization();
        TransactionSynchronizationManager.setActualTransactionActive(true);

        assertTrue(synchronizer.register(synchronization));

        final List<TransactionSynchronization> synchronizations = TransactionSynchronizationManager
                .getSynchronizations();
        assertEquals(1, synchronizations.size());
        assertSame(synchronization, synchronizations.get(0));
    }

    @Test
    public void testRegisterWithoutSynchronization() {
        TransactionSynchronizationManager.setActualTransactionActive(true);

        assertFalse(synchronizer.register(synchronization));
    }

    @Test
    public void testRegisterWithoutTransaction() {
        assertFalse(synchronizer.register(synchronization));
    }
}
