package com.pmi.tpd.spring.transaction;

import javax.annotation.Nonnull;

import org.springframework.transaction.support.TransactionSynchronization;

/**
 * A thin abstraction around Spring's {@code TransactionSynchronizationManager}.
 * <p>
 * The intention of this component is <i>not</i> to abstract away Spring; it's
 * to uses of
 * {@code TransactionSynchronizationManager} from spreading through the
 * codebase. Because
 * {@code TransactionSynchronizationManager} has a static API, it makes any code
 * that uses it very difficult to test.
 *
 * @author Christophe Friederich
 * @since 1.3
 */
public interface ITransactionSynchronizer {

  /**
   * Retrieves or creates {@link ITransactionalState state} that is associated
   * with the current transaction. This can
   * be used to track 'dirty' state in the current transaction, for instance to
   * implement transaction-aware caches.
   * <p>
   * The returned {@link ITransactionalState} always associates itself with the
   * transaction that is at that time
   * active, if any. If no transaction is active, the state will be empty and any
   * modifications of the state will be
   * ignored. As such, this method does not need to be called for every
   * transaction.
   *
   * @param namespace
   *                  the namespace identifying the piece of transactional state
   * @param <K>
   *                  the key type
   * @param <V>
   *                  the value type
   * @return the transactional state associated with the current transaction.
   */
  @Nonnull
  <K, V> ITransactionalState<K, V> getTransactionState(@Nonnull Object namespace);

  /**
   * @return {@code true} if a transaction is currently active
   */
  boolean isAvailable();

  /**
   * Registers the provided {@code TransactionSynchronization} callback, if a
   * transaction is active and
   * synchronization is available.
   *
   * @param synchronization
   *                        the callback to register, if a transaction is active
   * @return {@code true} if a transaction and synchronization are available and
   *         the provided callback was registered;
   *         otherwise, {@code false} if it wasn't
   */
  boolean register(@Nonnull TransactionSynchronization synchronization);
}
