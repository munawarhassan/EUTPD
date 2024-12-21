package com.pmi.tpd.euceg.core.filestorage;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.Optional;

import javax.annotation.Nonnull;

import com.google.common.io.CharSource;

/**
 * The interface represents the attachment stored in {@link com.pmi.tpd.core.euceg.spi.IFileStorage}.
 *
 * @see com.pmi.tpd.core.euceg.spi.IFileStorage
 * @author Christophe Friederich
 * @since 1.0
 */
public interface IFileStorageFile extends IFileStorageElement {

    /**
     * @return Returns <code>true</code> whether is temporary file, otherwise <code>false</code>.
     */
    boolean isTemporary();

    /**
     * @param charset
     *            the {@link Charset} to use.
     * @return Returns a {@link CharSource} view of this byte source that decodes bytes read from this source as
     *         characters using the given {@link Charset}.
     */
    @Nonnull
    CharSource asCharSource(Charset charset);

    /**
     * Retrieve an {@code InputStream} which can be used to read the attachment on disk.
     * <p>
     * Implementations shall return a distinct stream for each invocation. Callers should ensure the stream is always
     * closed after it has been used.
     *
     * @return a stream for reading the attachment
     * @throws IOException
     *             if the attachment cannot be opened for reading
     */
    @Nonnull
    InputStream openStream() throws IOException;

    /**
     * Retrieves the modification timestamp for the attachment, which represents the time the attachment was completed.
     *
     * @return the attachment's last modified timestamp
     */
    long getModified();

    /**
     * @return Returns the UUID representing the attachment.
     */
    @Nonnull
    String getUUID();

    /**
     * @return Returns the name of the file.
     */
    @Nonnull
    String getPhysicalName();

    /**
     * Gets the relative path.
     *
     * @return Returns a {@link Path} representing the path relative location to physical root location.
     */
    @Nonnull
    Path getPhysicalRelativePath();

    /**
     * Retrieves the size, in bytes, for the attachment.
     *
     * @return the attachment's size, in bytes
     */
    long getSize();

    /**
     * @return
     */
    @Nonnull
    Optional<String> getMimeType();

}
