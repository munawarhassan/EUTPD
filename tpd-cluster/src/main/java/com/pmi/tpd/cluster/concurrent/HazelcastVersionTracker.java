package com.pmi.tpd.cluster.concurrent;

import java.io.Serializable;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.hazelcast.map.EntryProcessor;
import com.hazelcast.map.IMap;

/**
 * @author Christophe Friederich
 * @since 1.3
 * @param <K>
 */
public class HazelcastVersionTracker<K extends Serializable> implements IVersionTracker<K> {

    /** */
    private final IMap<K, Integer> map;

    public HazelcastVersionTracker(final IMap<K, Integer> map) {
        this.map = map;
    }

    @Override
    public int get(@Nonnull final K key) {
        final Integer result = map.putIfAbsent(key, 0);
        return result == null ? 0 : result;
    }

    @Override
    public int incrementAndGet(@Nonnull final K key) {
        return (Integer) map.executeOnKey(key, new IncrementVersionProcessor<K>());
    }

    @Override
    public void increment(@Nonnull final K key) {
        incrementAndGet(key);
    }

    @Override
    public void incrementAll() {
        map.executeOnEntries(new IncrementVersionProcessor<K>());
    }

    private static class IncrementVersionProcessor<K extends Serializable>
            implements EntryProcessor<K, Integer, Object> {

        /**
         *
         */
        private static final long serialVersionUID = 1L;

        @Override
        public Integer process(final Map.Entry<K, Integer> entry) {
            Integer value = entry.getValue();
            value = value != null ? inc(value) : 1;
            entry.setValue(value);
            return value;
        }

        @Nullable
        @Override
        public EntryProcessor<K, Integer, Object> getBackupProcessor() {
            return this::processBackupEntry;
        }

        private Object processBackupEntry(final Entry<K, Integer> backupEntry) {
            return null;
        }

    }

    public static Integer inc(final Integer value) {
        return value == Integer.MAX_VALUE ? 0 : value + 1;
    }
}
