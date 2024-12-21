package com.pmi.tpd.api.exec;

import javax.annotation.Nonnull;

/**
 * Callback invoked when a maintenance task completes, whether it is {@link #onCancellation() canceled},
 * {@link #onSuccess() succeeds} or {@link #onFailure(Throwable) fails}.
 *
 * @author Christophe Friederich
 * @since 1.3
 */
@FunctionalInterface
public interface ICompletionCallback {

    /**
     * Indicates the maintenance task was canceled.
     */
    default void onCancellation() {
        onCompletion();
    }

    /**
     * Indicates the maintenance task failed for some reason.
     *
     * @param t
     *          the exception associated with the failure
     */
    default void onFailure(@Nonnull final Throwable t) {
        onCompletion();

    }

    /**
     * Indicated the maintenance task successfully completed.
     */
    default void onSuccess() {
        onCompletion();
    }

    void onCompletion();
}
