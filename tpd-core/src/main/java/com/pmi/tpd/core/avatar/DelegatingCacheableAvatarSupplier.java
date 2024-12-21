package com.pmi.tpd.core.avatar;

import java.io.IOException;
import java.io.InputStream;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.pmi.tpd.api.util.Assert;

/**
 * An {@link ICacheableAvatarSupplier} implementation which delegates to an {@link IAvatarSupplier}, storing the
 * avatar's {@link #getTimestamp() modification timestamp} separately. This class can be used to promote an ordinary
 * {@link IAvatarSupplier} to a {@link ICacheableAvatarSupplier}.
 * 
 * @since 2.4
 */
public class DelegatingCacheableAvatarSupplier implements ICacheableAvatarSupplier {

    private final IAvatarSupplier supplier;

    private final long timestamp;

    /**
     * Constructs a new {@code DelegatingCacheableAvatarSupplier} with an {@link #TIMESTAMP_UNKNOWN unknown} timestamp.
     *
     * @param supplier
     *                 the {@link IAvatarSupplier} containing the avatar's content type and {@code InputStream}
     * @see #DelegatingCacheableAvatarSupplier(IAvatarSupplier, long)
     * @see #TIMESTAMP_UNKNOWN
     */
    public DelegatingCacheableAvatarSupplier(@Nonnull final IAvatarSupplier supplier) {
        this(supplier, TIMESTAMP_UNKNOWN);
    }

    /**
     * Constructs a new {@code DelegatingCacheableAvatarSupplier} with the provided {@code timestamp}.
     *
     * @param supplier
     *                  the {@link IAvatarSupplier} containing the avatar's content type and {@code InputStream}
     * @param timestamp
     *                  the avatar's modification timestamp, which may be {@link #TIMESTAMP_UNKNOWN} if no modification
     *                  timestamp is known, or {@link #TIMESTAMP_ETERNAL} if the avatar is unmodifiable
     */
    public DelegatingCacheableAvatarSupplier(@Nonnull final IAvatarSupplier supplier, final long timestamp) {
        this.supplier = Assert.checkNotNull(supplier, "supplier");
        this.timestamp = timestamp;
    }

    @Override
    public String getContentType() {
        return supplier.getContentType();
    }

    @Nullable
    @Override
    public InputStream open() throws IOException {
        return supplier.open();
    }

    @Override
    public long getTimestamp() {
        return timestamp;
    }
}
