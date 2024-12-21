package com.pmi.tpd.service.testing.cluster;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.hazelcast.cp.IAtomicLong;
import com.hazelcast.cp.IAtomicReference;
import com.hazelcast.map.EntryProcessor;
import com.hazelcast.map.IMap;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class HazelcastTestUtils {

    public static IAtomicLong mockAtomicLong() {
        return createDelegatingMock(IAtomicLong.class, new AtomicLong());
    }

    public static <E> IAtomicReference<E> mockAtomicReference() {
        return createDelegatingMock(IAtomicReference.class, new AtomicReference());
    }

    public static <K, V> IMap<K, V> mockIMap(final Class<K> keyClass, final Class<V> valueClass) {
        final Map<K, V> backingMap = Maps.newConcurrentMap();
        final IMap<K, V> mock = createDelegatingMock(IMap.class, backingMap);

        Mockito.when(mock.executeOnKey(ArgumentMatchers.any(keyClass), ArgumentMatchers.any(EntryProcessor.class)))
                .thenAnswer(invocation -> {
                    final K key = (K) invocation.getArguments()[0];
                    final EntryProcessor<K, V, Object> processor = (EntryProcessor) invocation.getArguments()[1];

                    final Map.Entry<K, V> entry = Mockito.mock(Map.Entry.class);
                    Mockito.when(entry.getKey()).thenReturn(key);
                    Mockito.when(entry.getValue()).thenReturn(backingMap.get(key));
                    Mockito.when(entry.setValue(ArgumentMatchers.any(valueClass))).thenAnswer(invocation1 -> {
                        final V value = (V) invocation1.getArguments()[0];
                        if (value == null) {
                            return backingMap.remove(key);
                        }
                        return backingMap.put(key, value);
                    });
                    return processor.process(entry);
                });
        Mockito.doAnswer(invocation -> {
            backingMap.put((K) invocation.getArguments()[0], (V) invocation.getArguments()[1]);
            return null;
        }).when(mock).set(ArgumentMatchers.any(keyClass), ArgumentMatchers.any(valueClass));

        Mockito.doAnswer(invocation -> {
            final com.hazelcast.query.Predicate<K, V> predicate = (com.hazelcast.query.Predicate<K, V>) invocation
                    .getArguments()[0];
            return ImmutableSet.copyOf(Iterables.filter(backingMap.entrySet(), input -> predicate.apply(input)));
        }).when(mock).entrySet(ArgumentMatchers.any(com.hazelcast.query.Predicate.class));

        return mock;
    }

    private static <T> T createDelegatingMock(final Class<T> proxyType, final Object realTarget) {
        return Mockito.mock(proxyType, invocation -> {
            try {
                final Method method = realTarget.getClass()
                        .getMethod(invocation.getMethod().getName(), invocation.getMethod().getParameterTypes());
                return (T) method.invoke(realTarget, invocation.getArguments());
            } catch (final NoSuchMethodException e) {
            }
            return null;
        });
    }
}
