package com.pmi.tpd.api.lifecycle;

import javax.annotation.Nonnull;

import com.pmi.tpd.api.event.ICancelableEvent;
import com.pmi.tpd.api.i18n.KeyedMessage;

/**
 * Tracks the cancelation state of a cancelable operation.
 * <p>
 * Once an operation has been {@link #cancel(KeyedMessage) canceled}, it cannot
 * be "un-canceled". Where multiple
 * observers have the option to cancel an operation, cancellation by <i>any</i>
 * observer should be considered as final
 * and binding; implementations should not require consensus among all
 * observers.
 *
 * @author Christophe Friederich
 * @since 1.3
 */
public interface ICancelState {

  /**
   * Cancels the operation, providing a message explaining why.
   * <p>
   * The cancellation message is <i>required</i>, and should be as descriptive and
   * clear as possible to allow end
   * users to correct, if possible, the issue that triggered cancellation.
   *
   * @param message
   *                a descriptive message explaining why the operation has been
   *                canceled
   * @throws NullPointerException
   *                              if the provided {@code message} is {@code null}
   */
  void cancel(@Nonnull KeyedMessage message);

  /**
   * Retrieves a flag indicating whether the operation has been canceled.
   * <p>
   * In situations where multiple observers have the option to cancel an
   * operation, for example when multiple
   * listeners receive the same {@link ICancelableEvent CancelableEvent}, this
   * flag can be useful for determining that
   * another observer has already canceled the operation.
   *
   * @return {@code true} if the operation has been {@link #cancel(KeyedMessage)
   *         canceled}; otherwise, {@code false}
   */
  boolean isCanceled();
}
