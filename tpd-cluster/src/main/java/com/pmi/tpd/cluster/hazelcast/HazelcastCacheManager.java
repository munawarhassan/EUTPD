package com.pmi.tpd.cluster.hazelcast;

import static com.pmi.tpd.api.util.Assert.checkNotNull;

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.Callable;

import javax.annotation.Nonnull;

import org.springframework.cache.Cache;
import org.springframework.cache.Cache.ValueRetrievalException;
import org.springframework.cache.CacheManager;
import org.springframework.cache.support.SimpleValueWrapper;
import org.springframework.lang.Nullable;

import com.google.common.collect.Sets;
import com.hazelcast.core.DistributedObject;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.DataSerializable;

/**
 * Hazelcast implementation of the {@link CacheManager}. We're using this version instead of the Hazelcast-provided one
 * (in hazelcast-spring) because that version does not guarantee the {@code ClassLoader} that will be applied when
 * values are deserialized..
 *
 * @since 1.3
 */
public class HazelcastCacheManager implements CacheManager {

    /** */
    private static final DataSerializable NULL = new NullDataSerializable();

    /** */
    private final HazelcastInstance hazelcast;

    /**
     * Default construct. Create new instance of {@link HazelcastCacheManager}.
     *
     * @param hazelcast
     *                  a Hazelcast instance.
     */
    public HazelcastCacheManager(@Nonnull final HazelcastInstance hazelcast) {
        this.hazelcast = checkNotNull(hazelcast, "hazelcast");
    }

    /**
     * {@inheritDoc}
     */
    @Nonnull
    @Override
    public Cache getCache(@Nonnull final String name) {
        return new HazelcastCache(hazelcast.getMap(checkNotNull(name, "name")));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<String> getCacheNames() {
        final Set<String> names = Sets.newHashSet();
        for (final DistributedObject distributedObject : hazelcast.getDistributedObjects()) {
            if (distributedObject instanceof IMap) {
                names.add(distributedObject.getName());
            }
        }
        return names;
    }

    /**
     * @author Christophe Friederich
     * @since 1.3
     */
    private static final class HazelcastCache implements Cache {

        /** */
        private final IMap<Object, Object> map;

        private HazelcastCache(final IMap<Object, Object> map) {
            this.map = map;
        }

        @Override
        public void clear() {
            map.clear();
        }

        @Override
        public void evict(final Object key) {
            map.evict(key);
        }

        @Override
        public ValueWrapper get(final Object key) {
            return loadWrapper(key, null, true);
        }

        @Override
        @Nonnull
        public <T> T get(final Object key, final @Nullable Class<T> type) {
            return type.cast(fromStore(accessMap(key, null, true)));
        }

        @Override
        public String getName() {
            return map.getName();
        }

        @Override
        public Object getNativeCache() {
            return map;
        }

        @Override
        public void put(final Object key, final @Nullable Object value) {
            map.set(key, toStore(value));
        }

        @Override
        public ValueWrapper putIfAbsent(final Object key, final @Nullable Object value) {
            return loadWrapper(key, value, false);
        }

        private Object accessMap(final Object key, final Object value, final boolean get) {
            // Ensure the context class loader is always the host applications class loader
            // when retrieving
            // entries from the map as deserialisation uses it.
            final Thread currentThread = Thread.currentThread();
            final ClassLoader original = currentThread.getContextClassLoader();
            try {
                currentThread.setContextClassLoader(NullDataSerializable.class.getClassLoader());

                return get ? map.get(key) : map.putIfAbsent(key, toStore(value));
            } finally {
                currentThread.setContextClassLoader(original);
            }
        }

        private Object fromStore(final Object value) {
            return NULL.equals(value) ? null : value;
        }

        private ValueWrapper loadWrapper(final Object key, final Object value, final boolean get) {
            final Object result = accessMap(key, value, get);

            return result == null ? null : new SimpleValueWrapper(fromStore(result));
        }

        private Object toStore(final Object value) {
            return value == null ? NULL : value;
        }

        @SuppressWarnings("unchecked")
        @Override
        public <T> T get(final Object key, final Callable<T> valueLoader) {
            Object value = lookup(key);
            if (value != null) {
                return (T) fromStore(value);
            } else {
                this.map.lock(key);
                try {
                    value = lookup(key);
                    if (value != null) {
                        return (T) fromStore(value);
                    } else {
                        return loadValue(key, valueLoader);
                    }
                } finally {
                    this.map.unlock(key);
                }
            }
        }

        private <T> T loadValue(final Object key, final Callable<T> valueLoader) {
            T value;
            try {
                value = valueLoader.call();
            } catch (final Exception ex) {
                throw ValueRetrievalExceptionResolver.resolveException(key, valueLoader, ex);
            }
            put(key, value);
            return value;
        }

        private Object lookup(final Object key) {
            return this.map.get(key);
        }

    }

    /**
     * @author Christophe Friederich
     */
    private static class NullDataSerializable implements DataSerializable {

        @Override
        public boolean equals(final Object obj) {
            return obj != null && obj.getClass() == getClass();
        }

        @Override
        public int hashCode() {
            return 0;
        }

        @Override
        public void readData(final ObjectDataInput in) {
        }

        @Override
        public void writeData(final ObjectDataOutput out) {
        }
    }

    /**
     * @author Christophe Friederich
     * @since 1.5
     */
    private static class ValueRetrievalExceptionResolver {

        static RuntimeException resolveException(final Object key, final Callable<?> valueLoader, final Throwable ex) {
            return new ValueRetrievalException(key, valueLoader, ex);
        }
    }

}
