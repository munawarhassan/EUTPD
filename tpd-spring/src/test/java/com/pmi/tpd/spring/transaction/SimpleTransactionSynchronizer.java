package com.pmi.tpd.spring.transaction;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;

import org.springframework.transaction.support.TransactionSynchronization;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * Basic implementation of TransactionSynchronizer that can be used to test
 * transaction synchronization.
 */
public class SimpleTransactionSynchronizer implements ITransactionSynchronizer {

  private final Map<Object, MapTransactionalState<?, ?>> stateMap;

  private final List<TransactionSynchronization> synchronizations;

  private boolean transactionActive;

  private boolean transactionReadonly;

  public SimpleTransactionSynchronizer() {
    stateMap = Maps.newHashMap();
    synchronizations = Lists.newArrayList();
    transactionActive = false;
  }

  @Override
  public boolean isAvailable() {
    return transactionActive;
  }

  @Override
  public boolean register(@Nonnull final TransactionSynchronization synchronization) {
    if (transactionActive) {
      synchronizations.add(synchronization);
    }
    return false;
  }

  @Nonnull
  @Override
  public <K, V> ITransactionalState<K, V> getTransactionState(@Nonnull final Object namespace) {
    @SuppressWarnings("unchecked")
    MapTransactionalState<K, V> state = (MapTransactionalState<K, V>) stateMap.get(namespace);
    if (state == null) {
      state = new MapTransactionalState<>();
      stateMap.put(namespace, state);
    }
    return state;
  }

  public void transactionCommit() {
    try {
      for (final TransactionSynchronization synchronization : synchronizations) {
        synchronization.beforeCommit(transactionReadonly);
      }
      for (final TransactionSynchronization synchronization : synchronizations) {
        synchronization.beforeCompletion();
      }
      for (final TransactionSynchronization synchronization : synchronizations) {
        synchronization.afterCommit();
      }
    } finally {
      transactionComplete(TransactionSynchronization.STATUS_COMMITTED);
    }
  }

  public void transactionRollback() {
    try {
      for (final TransactionSynchronization synchronization : synchronizations) {
        synchronization.beforeCompletion();
      }
    } finally {
      transactionComplete(TransactionSynchronization.STATUS_ROLLED_BACK);
    }
  }

  public void transactionStart(final boolean readonly) {
    transactionReadonly = readonly;
    transactionActive = true;
  }

  private void transactionComplete(final int status) {
    try {
      for (final TransactionSynchronization synchronization : synchronizations) {
        synchronization.afterCompletion(status);
      }
    } finally {
      transactionActive = false;
      for (final MapTransactionalState<?, ?> state : stateMap.values()) {
        state.clear();
      }
    }
  }

  private static class MapTransactionalState<K, V> extends HashMap<K, V> implements ITransactionalState<K, V> {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
  }
}
