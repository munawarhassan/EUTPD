package com.pmi.tpd.testing.mockito;

import java.util.List;
import java.util.Map;

import org.mockito.ArgumentCaptor;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.google.common.collect.Lists;

public class MockitoUtils {

    public static <T> T getCaptorOfType(final Class<T> type, final ArgumentCaptor<?> captor) {
        for (final Object value : captor.getAllValues()) {
            if (value.getClass().isAssignableFrom(type)) {
                return type.cast(value);
            }
        }
        return null;
    }

    public static <T> List<T> getCaptorsOfType(final Class<T> type, final ArgumentCaptor<?> captor) {
        final List<T> values = Lists.newArrayList();
        for (final Object value : captor.getAllValues()) {
            if (value.getClass().isAssignableFrom(type)) {
                values.add(type.cast(value));
            }
        }
        return values;
    }

    public static <K, V> Answer<Void> putsValueInMap(final Map<K, V> map) {
        return putsValueInMap(map, 0, 1);
    }

    @SuppressWarnings("unchecked")
    public static <K, V> Answer<Void> putsValueInMap(final Map<K, V> map, final int keyIndex, final int valueIndex) {
        return invocation -> {
            map.put((K) invocation.getArguments()[keyIndex], (V) invocation.getArguments()[valueIndex]);
            return null;
        };
    }

    public static <K, V> Answer<Void> removesValueInMap(final Map<K, V> map) {
        return removesValueInMap(map, 0);
    }

    @SuppressWarnings({ "SuspiciousMethodCalls" })
    public static <K, V> Answer<Void> removesValueInMap(final Map<K, V> map, final int keyIndex) {
        return invocation -> {
            map.remove(invocation.getArguments()[keyIndex]);
            return null;
        };
    }

    public static <T> Answer<T> returnArg(final int index) {
        return new Answer<>() {

            @SuppressWarnings("unchecked")
            @Override
            public T answer(final InvocationOnMock invocationOnMock) throws Throwable {
                return (T) invocationOnMock.getArguments()[index];
            }
        };
    }

    /**
     * @param <T>
     *            the return type of the Answer
     * @return an Answer that returns the first argument
     */
    public static <T> Answer<T> returnFirst() {
        return returnArg(0);
    }

    public static SelfAnswer returnSelf(final Class<?> mockType) {
        return new SelfAnswer(mockType);
    }

    public static SelfAnswer returnSelf(final Class<?> mockType, final Answer<Object> defaultAnswer) {
        return new SelfAnswer(mockType, defaultAnswer);
    }

    public static Answer<Object> returnsSelf() {
        return invocation -> invocation.getMock();
    }

    public static <K, V> Answer<V> returnsValueFromMap(final Map<K, V> map) {
        return returnsValueFromMap(map, 0);
    }

    @SuppressWarnings("SuspiciousMethodCalls")
    public static <K, V> Answer<V> returnsValueFromMap(final Map<K, V> map, final int keyIndex) {
        return invocation -> map.get(invocation.getArguments()[keyIndex]);
    }

}
