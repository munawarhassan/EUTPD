package com.pmi.tpd.spring.transaction;

import static com.google.common.base.Preconditions.checkState;

import java.util.HashMap;
import java.util.Map;

import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * {@link ITransactionalState} implementation that stores the state as a transactional resource through Spring's
 * {@link TransactionSynchronizationManager}.
 *
 * @author Christophe Friederich
 * @since 1.3
 */
class DefaultTransactionalState<K, V> implements ITransactionalState<K, V> {

    /** */
    private final Object namespace;

    DefaultTransactionalState(final Object namespace) {
        this.namespace = namespace;
    }

    @SuppressWarnings("unchecked")
    @Override
    public V get(final K key) {
        final Object resource = TransactionSynchronizationManager.getResource(namespace);
        if (resource instanceof Map) {

            final Map<K, V> txMap = (Map<K, V>) resource;
            return txMap.get(key);
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    @Override
    public V put(final K key, final V value) {
        checkState(TransactionSynchronizationManager.isSynchronizationActive(), "There is no active transaction");
        final Object resource = TransactionSynchronizationManager.getResource(namespace);
        Map<K, V> txMap;
        if (resource == null) {
            // does not have to be a concurrent map, is only going to be used on the current thread anyhow
            txMap = new HashMap<>();
            TransactionSynchronizationManager.bindResource(namespace, txMap);
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {

                @Override
                public void afterCompletion(final int status) {
                    clear();
                }
            });
        } else if (resource instanceof Map) {
            txMap = (Map<K, V>) resource;
        } else {
            throw new IllegalStateException(
                    "The transactional resource is not a map! (" + resource.getClass().getName() + ")");
        }

        return txMap.put(key, value);
    }

    @Override
    public void clear() {
        TransactionSynchronizationManager.unbindResourceIfPossible(namespace);
    }
}
