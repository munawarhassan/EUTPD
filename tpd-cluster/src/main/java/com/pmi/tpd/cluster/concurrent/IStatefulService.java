package com.pmi.tpd.cluster.concurrent;

import javax.annotation.Nonnull;

import com.google.common.base.Function;

/**
 * Decorator interface which should be implemented by services which have state that should be transferred to
 * {@code Executor}s when work is delegated to another thread.
 *
 * @author Christophe Friederich
 * @since 1.3
 */
public interface IStatefulService {

    /**
     * Helper function to retrieve the TransferableState that is provided by a StatefulService.
     */
    Function<IStatefulService, ITransferableState> TO_STATE = IStatefulService::getState;

    /**
     * Retrieves a snapshot of the <i>current</i> state of the service. The state should be fully realised in the
     * returned object--any steps which are necessary to "detach" the state and prepare it to be applied on another
     * thread must have already been performed. The amount of time between the invocation of this method on the original
     * thread and the invocation of {@link ITransferableState#apply() apply} on the worker thread is undefined, so if
     * the state is still attached in any way to the original thread it may result in unexpected (and very difficult to
     * debug) behaviour on the worker thread.
     * <p>
     * Note: If a batch of tasks is submitted from the original thread, rather than just a single task, this method will
     * be called once for each task in the batch and the state returned from each call will be applied to exactly
     * <i>one</i> worker thread.
     *
     * @return a snapshot of the service's current thread state
     */
    @Nonnull
    ITransferableState getState();
}
