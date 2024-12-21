package com.pmi.tpd.api.exec;

/**
 * Marker interface for exceptions thrown when a {@link IRunnableTask} is canceled.
 * <p>
 * The goal of this interface is to simplify detecting whether a maintenance task that was canceled completed anyway,
 * failed in a way unrelated to being canceled, or actually detected that it was canceled and aborted. This interface
 * should be applied to the exception thrown for the latter case, where maintenance detected the cancellation request
 * and aborted itself.
 * <p>
 * The granularity of cancellation checks within maintenance tasks means that, even if the user cancels maintenance, the
 * system may never detect that before it either completes processing or the processing fails. Either way, it is
 * important when messaging the outcome of the maintenance to be able to differentiate.
 *
 * @author Christophe Friederich
 * @since 1.3
 */
public interface IRunnableTaskCanceled {

    // Adds no methods; this interface is just a marker
}
