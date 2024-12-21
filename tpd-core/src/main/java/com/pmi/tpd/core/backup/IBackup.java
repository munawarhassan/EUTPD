package com.pmi.tpd.core.backup;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

import javax.annotation.Nonnull;

import com.google.common.io.CharSource;

/**
 * Represents a single system backup.
 *
 * @author Christophe Friederich
 * @since 1.3
 */
public interface IBackup {

  /**
   * Returns a {@link CharSource} view of this byte source that decodes bytes read
   * from this source as characters
   * using the given {@link Charset}.
   */
  CharSource asCharSource(Charset charset);

  /**
   * Retrieve an {@code InputStream} which can be used to read the backup on disk.
   * <p>
   * Implementations shall return a distinct stream for each invocation. Callers
   * should ensure the stream is always
   * closed after it has been used.
   *
   * @return a stream for reading the backup
   * @throws IOException
   *                     if the backup cannot be opened for reading
   */
  @Nonnull
  InputStream openStream() throws IOException;

  /**
   * Retrieves the modification timestamp for the backup, which represents the
   * time the backup was completed.
   *
   * @return the backup's last modified timestamp
   */
  long getModified();

  /**
   * Retrieves the name for this backup, which will be unique across all backups.
   *
   * @return the backup's name
   */
  @Nonnull
  String getName();

  /**
   * Retrieves the size, in bytes, for the backup.
   *
   * @return the backup's size, in bytes
   */
  long getSize();
}
