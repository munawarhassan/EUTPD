package com.pmi.tpd.cluster.spring;

import java.lang.reflect.Method;
import java.util.concurrent.CountDownLatch;

import org.springframework.core.InfrastructureProxy;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.pmi.tpd.cluster.latch.LatchedInvocationHandler;

/**
 * A {@link LatchedInvocationHandler} implementation which does not block existing transactions on the target resource.
 * The goal of this implementation is to prevent deadlocking existing transactions, which, in turn, prevent the
 * {@code DataSource} from being able to drain, preventing backup or migration from running.
 * 
 * @author Christophe Friederich
 * @since 1.3
 */
public class TransactionAwareLatchedInvocationHandler extends LatchedInvocationHandler implements InfrastructureProxy {

    /** */
    private final Object transactionDelegate;

    /**
     * Constructs a new {@code TransactionAwareLatchedInvocationHandler} which will block on the provided latch before
     * forwarding invocations to the specified {@code targetDelegate}.
     * <p>
     * Before latching, invocations will {@link #isInTransaction() check for transactions} already in process on the
     * {@code transactionDelegate}. If a transaction exists, the invocation will be handled by the transaction delegate
     * instead of by the {@code targetDelegate}.
     *
     * @param targetDelegate
     *            the delegate to invoke when the latch is released
     * @param latch
     *            the countdown latch to await before invoking the target delegate
     * @param transactionDelegate
     *            the resource to use for existing transactions.
     * @see LatchedInvocationHandler#LatchedInvocationHandler(Object, CountDownLatch) for more details.
     */
    public TransactionAwareLatchedInvocationHandler(final Object targetDelegate, final CountDownLatch latch,
            final Object transactionDelegate) {
        super(targetDelegate, latch);

        this.transactionDelegate = transactionDelegate;
    }

    @Override
    public Object getWrappedObject() {
        final Object delegate = getDelegate(null);

        return delegate instanceof InfrastructureProxy ? ((InfrastructureProxy) delegate).getWrappedObject() : delegate;
    }

    @Override
    protected Object getDelegate(final Method method) {
        // There is no guarantee that either of the delegates implement {@code InfrastructureProxy}
        // and we also never want the proxy to be used within spring's transaction management
        // so we need to manually handle the methods for {@code InfrastructureProxy}
        if (method != null && method.getDeclaringClass() == InfrastructureProxy.class) {
            return this;
        }

        return isInTransaction() ? transactionDelegate : super.getDelegate(method);
    }

    @Override
    protected boolean isLatched(final Method method) {
        return super.isLatched(method) && !isInTransaction();
    }

    /**
     * @return {@code true} if a transaction is associated with the {@code transactionDelegate} for the current thread;
     */
    private boolean isInTransaction() {
        return TransactionSynchronizationManager.hasResource(transactionDelegate);
    }
}
