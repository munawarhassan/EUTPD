package com.pmi.tpd.cluster.latch;

import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hazelcast.cluster.Member;
import com.hazelcast.core.MultiExecutionCallback;

/**
 * A {@link MultiExecutionCallback} which.
 * <ul>
 * <li>Will collect the results from the execution on each node (and determine if it was successful)</li>
 * <li>Will notify (via latch) interested parties once all nodes have finished executing</li>
 * </ul>
 *
 * @author Christophe Friederich
 * @since 1.3
 */
public abstract class ResultCollectingExecutionCallback<T> implements MultiExecutionCallback {

    /** */
    private final Logger logger = LoggerFactory.getLogger(getClass());

    /** */
    private final CountDownLatch completeLatch = new CountDownLatch(1);

    /** */
    protected final AtomicBoolean status = new AtomicBoolean(true);

    @SuppressWarnings("unchecked")
    @Override
    public void onResponse(final Member member, final Object o) {
        if (o instanceof Throwable) {
            logger.warn("Error while executing on {}", member, o);
            status.compareAndSet(true, false);
            onError(member, (Throwable) o);
        } else {
            logger.debug("Success executing on {}. Result: {}", member, o);
            onSuccess(member, (T) o);
        }
    }

    @Override
    public void onComplete(final Map<Member, Object> memberObjectMap) {
        logger.debug("Execution complete. Successful - {}", isSuccess());
        completeLatch.countDown();
    }

    public void await() throws InterruptedException {
        completeLatch.await();
    }

    public boolean await(final long timeout, final TimeUnit timeUnit) throws InterruptedException {
        return completeLatch.await(timeout, timeUnit);
    }

    public boolean isSuccess() {
        return status.get();
    }

    protected void onError(final Member member, final Throwable throwable) {
    }

    protected void onSuccess(final Member member, final T value) {
        onSuccess(value);
    }

    protected void onSuccess(final T value) {
    }
}
