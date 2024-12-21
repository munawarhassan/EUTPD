package com.pmi.tpd.cluster.util;

public final class OsgiSafeUtils {

    private OsgiSafeUtils() {
        // to prevent instantiation
    }

    public static <T> T unwrap(final IOsgiSafe<T> value) {
        return value == null ? null : value.getValue();
    }

    public static <T> IOsgiSafe<T> wrap(final T value) {
        return new IOsgiSafe<>(value);
    }
}
