package com.pmi.tpd.core.avatar;

import java.io.IOException;
import java.io.InputStream;

import javax.annotation.Nullable;

/**
 * @since 2.4
 */
public interface IAvatarSupplier {

    /**
     * The content type of the avatar, e.g., "image/png"
     *
     * @return the string representing the content type
     */
    @Nullable
    String getContentType();

    /**
     * Provides an {@code InputStream} to read the avatar. Each call to this method will produce a <i>new</i> stream.
     *
     * @return a stream from the avatar
     * @throws IOException
     *                     in case the avatar could not be opened or read
     */
    @Nullable
    InputStream open() throws IOException;
}
