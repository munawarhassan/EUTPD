package com.pmi.tpd.spring.transaction;

/**
 * A minimal map representing state associated with the current transaction, if any. {@code TransactionalState} always
 * associates itself with the current transaction and can be seen as a thread-local Map that turns into an immutable
 * empty map when no transaction is active.
 *
 * @param <K>
 *            key type
 * @param <V>
 *            value type
 */
public interface ITransactionalState<K, V> {

    /**
     * Clears the transactional state.
     */
    void clear();

    /**
     * @param key
     *            the key
     * @return the value if a transaction is active and state under {@code key} is associated with the current
     *         transaction, otherwise {@code null}
     */
    V get(K key);

    /**
     * Associates a bit of state with the current transaction. A no-op if no transaction is active.
     *
     * @param key
     *            the key
     * @param value
     *            the value
     * @return the previous value if any, otherwise {@code null}
     */
    V put(K key, V value);
}
