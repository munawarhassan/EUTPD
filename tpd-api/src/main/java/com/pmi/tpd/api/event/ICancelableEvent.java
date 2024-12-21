package com.pmi.tpd.api.event;

import com.pmi.tpd.api.lifecycle.ICancelState;

/**
 * Augments an event with support for cancelation.
 * <p>
 * When a cancelable event is raised, <i>any listener</i> may choose to cancel the operation. A descriptive message must
 * be provided, explaining why the operation was canceled. If <i>any</i> listener cancels the operation, it will not be
 * performed--even if other listeners did not cancel. In other words, if three listeners receive the event and only one
 * of them cancels, the associated operation will be considered <i>canceled</i>.
 *
 * @since 2.0
 */
public interface ICancelableEvent extends ICancelState {
}
