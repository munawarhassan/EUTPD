package com.pmi.tpd.core.util;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Map;
import java.util.function.Supplier;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.springframework.web.servlet.support.RequestContext;

import com.google.common.collect.ForwardingMap;
import com.google.common.collect.Maps;
import com.pmi.tpd.web.core.request.spi.IRequestContext;

/**
 * Allows for caching values against the current request, if there is any. No caching occurs when invoked without
 * request context.
 */
public class RequestLocalCache<K, V> {

    private final IRequestContext requestContext;

    private final ThreadLocal<Map<K, V>> cache;

    public RequestLocalCache(final IRequestContext requestContext) {
        this.requestContext = requestContext;
        this.cache = new ThreadLocal<>() {

            @Override
            protected Map<K, V> initialValue() {
                // this will _only_ get invoked if there's active request context (see get())
                requestContext.addCleanupCallback(() -> cache.remove());

                return Maps.newHashMap();
            }
        };
    }

    /**
     * Get the value mapped to {@code key}, or load it using {@code loader} if the value is not cached yet, or if there
     * is no active request to cache the value against. If the method was called in an {@link RequestContext#isActive()
     * active request context}, the returned value is guaranteed to be cached against the {@code key} after this method
     * has completed.
     * <p>
     * Note: Nullability for the return value is intentionally not documented one way or another. It is expected that
     * the <i>caller</i> will know the nullability of {@code loader} they are passing and code accordingly.
     *
     * @param key
     *            key
     * @param loader
     *            value loader
     * @return the cached value that has been obtained from {@code loader} during this, or one of the previous
     *         invocations
     */
    public V get(@Nonnull final K key, @Nonnull final Supplier<V> loader) {
        checkNotNull(key, "key");
        checkNotNull(loader, "loader");

        if (requestContext.isActive() && cache.get().containsKey(key)) {
            return cache.get().get(key);
        } else {
            final V value = loader.get();
            if (requestContext.isActive()) {
                cache.get().put(key, value);
            }
            return value;
        }
    }

    /**
     * Gets the {@link Map} that backs this Cache if the method was called in an {@link RequestContext#isActive() active
     * request context}. Otherwise @{code null} is returned.
     *
     * @return the {@link Map} backing this cache or {@code null}
     */
    @Nullable
    public Map<K, V> asMap() {
        if (requestContext.isActive()) {
            return new ForwardingMap<>() {

                @Override
                protected Map<K, V> delegate() {
                    if (!requestContext.isActive()) {
                        throw new IllegalStateException("Not in a request scope!");
                    }
                    return cache.get();
                }
            };
        }
        return null;
    }
}
