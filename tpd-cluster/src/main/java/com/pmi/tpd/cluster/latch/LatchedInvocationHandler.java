package com.pmi.tpd.cluster.latch;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.concurrent.CountDownLatch;

import org.springframework.util.ReflectionUtils;

/**
 * An implementation of {@code InvocationHandler} which awaits a provided {@code CountDownLatch} before dispatching
 * invocations to a provided delegate {@code Object}.
 * 
 * @author Christophe Friederich
 * @since 1.3
 */
public class LatchedInvocationHandler implements InvocationHandler {

    /** */
    private final Object delegate;

    /** */
    private final CountDownLatch latch;

    /**
     * Constructs a new {@code LatchedInvocationHandler} which will dispatch invoked methods to the provided
     * {@code delegate} after awaiting the provided {@code latch}.
     *
     * @param delegate
     *            the target instance to receive method invocations
     * @param latch
     *            the latch to await before dispatching invocations
     */
    public LatchedInvocationHandler(final Object delegate, final CountDownLatch latch) {
        this.delegate = delegate;
        this.latch = latch;
    }

    /**
     * Awaits the {@code CountDownLatch} provided at construction and then invokes the method on the target delegate.
     * <p>
     * If the method being invoked is declared by {@code Object}, it is immediately forwarded to the delegate without
     * awaiting the latch. This is done primarily to facilitate debugging, since IDEs invoke {@code toString()} on
     * objects whenever they are paused at a breakpoint. If that call blocks, the IDE hangs.
     *
     * @param proxy
     *            the proxy instance backed by this handler
     * @param method
     *            the method being invoked on the proxy
     * @param args
     *            the arguments to the method call
     * @return the result of the method invocation, performed after the {@code latch.await()} returns
     * @throws Throwable
     *             if an exception is thrown by the {@link #getDelegate(Method) delegate} during invocation
     */
    @Override
    public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
        if (isLatched(method)) {
            latch.await();
        }

        return ReflectionUtils.invokeMethod(method, getDelegate(method), args);
    }

    /**
     * @param method
     *            the method being invoked, which may be {@code null} if called outside the context of
     *            {@link #invoke(Object, Method, Object[])}
     * @return the instance to delegate to
     */
    protected Object getDelegate(final Method method) {
        return delegate;
    }

    /**
     * @param method
     *            the method being invoked, can not be {@code null}
     * @return whether the method invocation should await the latch
     */
    protected boolean isLatched(final Method method) {
        // This allows calls like toString() and hashCode() to go through, which is important for the debugger.
        // Note, however, that if the proxied interface overrides the method (for example, it declares toString()
        // even though Object mandates it), this will no longer return true and the call will block.
        return method.getDeclaringClass() != Object.class;
    }
}
