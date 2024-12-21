package com.pmi.tpd.core.bootstrap;

import static org.mockito.ArgumentMatchers.anyString;

import java.util.InputMismatchException;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.annotation.Nonnull;
import javax.sql.DataSource;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Spy;

import com.google.common.collect.Maps;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.cp.CPSubsystem;
import com.hazelcast.cp.lock.FencedLock;
import com.pmi.tpd.api.i18n.I18nService;
import com.pmi.tpd.api.i18n.support.SimpleI18nService;
import com.pmi.tpd.api.util.IUncheckedOperation;
import com.pmi.tpd.cluster.concurrent.IClusterLock;
import com.pmi.tpd.cluster.concurrent.IClusterLockService;
import com.pmi.tpd.cluster.concurrent.LockException;
import com.pmi.tpd.testing.junit5.MockitoTestCase;

import liquibase.database.Database;

public class DefaultLockServiceTest extends MockitoTestCase {

    @Mock(lenient = true)
    private IClusterLockService clusterLockService;

    @Mock
    private IClusterLock clusterLock;

    @Captor
    private ArgumentCaptor<String> lockNameCaptor;

    @Mock
    private IUncheckedOperation<Void> operation;

    @Mock(lenient = true)
    private HazelcastInstance hazelcast;

    @Mock(lenient = true)
    private CPSubsystem cpSubsystem;

    @Mock
    private DataSource dataSource;

    @Mock(lenient = true)
    private liquibase.lockservice.LockService liquibaseLockService;

    @Mock
    private Database database;

    @Spy
    private final I18nService i18nService = new SimpleI18nService();

    private DefaultLockService lockService;

    @BeforeEach
    public void setUp() throws Exception {
        when(hazelcast.getCPSubsystem()).thenReturn(cpSubsystem);
        doAnswer(invocation -> null).when(liquibaseLockService).waitForLock();

        doAnswer(invocation -> null).when(liquibaseLockService).releaseLock();

        lockService = new DefaultLockService(clusterLockService, dataSource, i18nService) {

            @Nonnull
            @Override
            public IBootstrapLock getBootstrapLock() {
                return new LiquibaseBootstrapLock(dataSource, i18nService) {

                    @Nonnull
                    @Override
                    Database findDatabase(@Nonnull final DataSource dataSource) {
                        return database;
                    }

                    @Nonnull
                    @Override
                    liquibase.lockservice.LockService getLockService(@Nonnull final Database database) {
                        return liquibaseLockService;
                    }
                };
            }
        };

        final Map<String, FencedLock> locks = Maps.newHashMap();
        when(hazelcast.getCPSubsystem().getLock(anyString())).thenAnswer(invocation -> {
            final String lockName = (String) invocation.getArguments()[0];
            synchronized (locks) {
                if (!locks.containsKey(lockName)) {
                    locks.put(lockName, mockLock());
                }
            }
            return locks.get(lockName);
        });
        when(clusterLockService.getLockForName(anyString())).thenReturn(clusterLock);
    }

    @Test
    public void testGetLock() {
        final Lock lock = lockService.getLock("test");
        assertSame(clusterLock, lock);
        verify(clusterLockService).getLockForName(lockNameCaptor.capture());

        assertTrue(lockNameCaptor.getValue().endsWith("test"));
    }

    @Test
    public void testBootstrapLockOperationExecutes() throws Throwable {
        lockService.getBootstrapLock().withLock(operation);

        final InOrder inOrder = inOrder(operation, liquibaseLockService, database);

        inOrder.verify(liquibaseLockService).waitForLock();
        inOrder.verify(operation).perform();
        inOrder.verify(liquibaseLockService).releaseLock();
        inOrder.verify(database).close();
    }

    @Test
    public void testBootstrapLockOperationThrowsException() throws Throwable {
        assertThrows(InputMismatchException.class, () -> {
            try {
                lockService.getBootstrapLock().withLock(() -> {
                    throw new InputMismatchException("Some very specific exception");
                });
            } finally {
                final InOrder inOrder = inOrder(liquibaseLockService, database);
                inOrder.verify(liquibaseLockService).waitForLock();
                inOrder.verify(liquibaseLockService).releaseLock();
                inOrder.verify(database).close();
            }
        });
    }

    @Test
    public void testBootstrapLockAcquiringLockThrowsException() throws Throwable {
        assertThrows(LockException.class, () -> {
            doThrow(liquibase.exception.LockException.class).when(liquibaseLockService).waitForLock();

            try {
                lockService.getBootstrapLock().withLock(() -> {
                    throw new RuntimeException("never reaches here");
                });
            } finally {
                verify(liquibaseLockService).waitForLock();
                verify(liquibaseLockService, never()).releaseLock();
            }
        });
    }

    // Currently the LockException is thrown, as opposed to the operation exception.
    @Test
    public void testBootstrapLockReleaseLockThrowsException() throws Throwable {
        assertThrows(LockException.class, () -> {
            doThrow(liquibase.exception.LockException.class).when(liquibaseLockService).releaseLock();

            try {
                lockService.getBootstrapLock().withLock(() -> {
                    throw new RuntimeException();
                });
            } finally {
                final InOrder inOrder = inOrder(operation, liquibaseLockService, database);

                inOrder.verify(liquibaseLockService).waitForLock();
                inOrder.verify(liquibaseLockService).releaseLock();
                inOrder.verify(database).close();
            }
        });
    }

    // creates a mock ILock, backed by a fair ReentrantLock for the lock and unlock operations. Hazelcast provided
    // locks have the same characteristics.
    private FencedLock mockLock() {
        final ReentrantLock lock = new ReentrantLock(true);
        final FencedLock mock = mock(FencedLock.class);
        doAnswer(invocation -> {
            lock.lock();
            return null;
        }).when(mock).lock();

        doAnswer(invocation -> {
            lock.unlock();
            return null;
        }).when(mock).unlock();

        return mock;
    }
}
