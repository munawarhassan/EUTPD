package com.pmi.tpd.core.euceg.report;

import java.io.IOException;
import java.io.InputStream;

import javax.annotation.Nonnull;

import com.google.common.io.ByteSource;

public interface ITrackingReport {

    /**
     * Returns a {@link ByteSource} view of this byte source.
     */
    ByteSource asByteSource();

    /**
     * Retrieve an {@code InputStream} which can be used to read the tracking report on disk.
     * <p>
     * Implementations shall return a distinct stream for each invocation. Callers should ensure the stream is always
     * closed after it has been used.
     *
     * @return a stream for reading the report tracking
     * @throws IOException
     *                     if the backup cannot be opened for reading
     */
    @Nonnull
    InputStream openStream() throws IOException;

    /**
     * Retrieves the modification timestamp for the report tracking, which represents the time the report tracking was
     * completed.
     *
     * @return the report tracking's last modified timestamp
     */
    long getModified();

    /**
     * @return
     */
    @Nonnull
    String getId();

    /**
     * @return
     */
    @Nonnull
    String getUsername();

    /**
     * Retrieves the name for this report tracking, which will be unique across all backups.
     *
     * @return the backup's name
     */
    @Nonnull
    String getName();

    @Nonnull
    String getType();

    /**
     * Retrieves the size, in bytes, for the report tracking.
     *
     * @return the report tracking's size, in bytes
     */
    long getSize();

    /**
     * @return
     */
    String getContentType();

}
