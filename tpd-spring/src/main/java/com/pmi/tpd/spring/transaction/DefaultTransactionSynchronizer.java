package com.pmi.tpd.spring.transaction;

import static com.google.common.base.Preconditions.checkNotNull;

import javax.annotation.Nonnull;

import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * A default implementation of {@link ITransactionSynchronizer} which uses the
 * static API for
 * {@code TransactionSynchronizationManager} to register synchronizations.
 *
 * @author
 * @since 1.3
 */
public class DefaultTransactionSynchronizer implements ITransactionSynchronizer {

  @Nonnull
  @Override
  public <K, V> ITransactionalState<K, V> getTransactionState(@Nonnull final Object namespace) {
    return new DefaultTransactionalState<>(checkNotNull(namespace, "namespace"));
  }

  @Override
  public boolean isAvailable() {
    return TransactionSynchronizationManager.isActualTransactionActive()
        && TransactionSynchronizationManager.isSynchronizationActive(); // Should always be true,
                                                                        // but...
  }

  @Override
  public boolean register(@Nonnull final TransactionSynchronization synchronization) {
    if (isAvailable()) {
      TransactionSynchronizationManager.registerSynchronization(checkNotNull(synchronization, "synchronization"));

      return true;
    }

    return false;
  }
}
