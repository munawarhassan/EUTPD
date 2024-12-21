package com.pmi.tpd.core.avatar;

import java.io.InputStream;

import javax.annotation.Nonnull;

import com.pmi.tpd.api.util.Assert;

/**
 * A simple implementation of {@link IAvatarSupplier} which accepts the {@code InputStream} containing the avatar's
 * image data as a constructor parameter.
 * 
 * @since 2.4
 */
public class SimpleAvatarSupplier extends AbstractAvatarSupplier {

    /** */
    @Nonnull
    private final InputStream inputStream;

    /**
     * Constructs a new {@code SimpleAvatarSupplier} which will {@link #open() return} the provided {@code InputStream}.
     *
     * @param inputStream
     *                    the input stream for this supplier
     * @throws NullPointerException
     *                              if the provided {@code inputStream} is {@code null}
     */
    public SimpleAvatarSupplier(@Nonnull final InputStream inputStream) {
        this(null, inputStream);
    }

    /**
     * Constructs a new {@code SimpleAvatarStream} with the specified {@code contentType} which will {@link #open()
     * return} the provided {@code InputStream}.
     *
     * @param contentType
     *                    the declared content type for the avatar, which may be {@code null} if not known
     * @param inputStream
     *                    the input stream for this supplier
     * @throws NullPointerException
     *                              if the provided {@code inputStream} is {@code null}
     */
    public SimpleAvatarSupplier(final String contentType, @Nonnull final InputStream inputStream) {
        super(contentType);

        this.inputStream = Assert.checkNotNull(inputStream, "inputStream");
    }

    /**
     * Retrieves the {@code InputStream} provided when this supplier was constructed.
     *
     * @return the input stream containing the avatar data
     */
    @Nonnull
    @Override
    public InputStream open() {
        return inputStream;
    }
}
