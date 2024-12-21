package com.pmi.tpd.cluster.concurrent;

/**
 * A snapshot of the state of a given {@link IStatefulService} at the time of its creation.
 * <p>
 * State is <i>required</i> to be reusable. Each instance of this interface will be applied to <i>at least</i> one
 * thread, but may be applied to more. As a result, implementations of this interface must be thread-safe, and they need
 * to handle the situation where applying state on multiple threads requires that state to be renewed or otherwise
 * refreshed.
 * 
 * @author Christophe Friederich
 * @since 1.3
 */
public interface ITransferableState {

    /**
     * Applies the state from the snapshot to the current thread.
     * <p>
     * When this method returns, from the point of view of the service from which this state was drawn, the thread to
     * which the state was applied should be <i>indistinguishable</i> from the originating thread.
     * <p>
     * Implementation Note: This method is guaranteed to be called <i>at least</i> once, on a different thread than the
     * originating thread, but no upper bound is guaranteed. Neither is the duration of the delay between then the state
     * was constructed and when it is applied.
     */
    void apply();

    /**
     * Removes any state applied from the snapshot.
     * <p>
     * When this method returns, from the point of view of the service from which this state was drawn, the thread from
     * which the state was removed should appear as if it never held state. This means any {@code ThreadLocal}s or other
     * properties which were set on the worker thread must all be cleared completely.
     * <p>
     * Implementation Note: This method is guaranteed to be called <i>at least</i> once, on a different thread than the
     * originating thread, but no upper bound is guaranteed. Neither is the duration of the delay between then the state
     * was constructed and when it is applied.
     */
    void remove();
}
