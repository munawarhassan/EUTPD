package com.pmi.tpd.util;

import javax.inject.Provider;

import com.google.common.base.Objects;

public final class Providers {

    private Providers() {
    }

    /**
     * Returns a provider which always provides {@code instance}. This should not be necessary to use in your
     * application, but is helpful for several types of unit tests.
     *
     * @param instance
     *            the instance that should always be provided. This is also permitted to be null, to enable aggressive
     *            testing, although in real life a Guice-supplied Provider will never return null.
     */
    public static <T> Provider<T> of(final T instance) {
        return new ConstantProvider<>(instance);
    }

    private static class ConstantProvider<T> implements Provider<T> {

        private final T instance;

        private ConstantProvider(final T instance) {
            this.instance = instance;
        }

        @Override
        public T get() {
            return instance;
        }

        @Override
        public String toString() {
            return "of(" + instance + ")";
        }

        @Override
        public boolean equals(final Object obj) {
            return obj instanceof ConstantProvider && Objects.equal(instance, ((ConstantProvider<?>) obj).instance);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(instance);
        }
    }

}
