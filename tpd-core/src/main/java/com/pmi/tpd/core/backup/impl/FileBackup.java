package com.pmi.tpd.core.backup.impl;

import static com.pmi.tpd.api.util.Assert.checkNotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.annotation.Nonnull;

import com.google.common.base.Function;
import com.google.common.io.ByteSource;
import com.pmi.tpd.core.backup.IBackup;

/**
 * A simple {@link IBackup} implementation wrapping a {@code File}.
 *
 * @author Christophe Friederich
 * @since 1.3
 */
public class FileBackup extends ByteSource implements IBackup {

  /** */
  public static final Function<File, IBackup> FILE_TRANSFORM = new Function<File, IBackup>() {

    @Override
    public FileBackup apply(final File file) {
      return new FileBackup(file);
    }
  };

  /** */
  private final File file;

  public FileBackup(@Nonnull final File file) {
    this.file = checkNotNull(file, "file");
  }

  @Override
  public long getModified() {
    return file.lastModified();
  }

  @Nonnull
  @Override
  public String getName() {
    return file.getName();
  }

  @Override
  public long getSize() {
    return file.length();
  }

  @Override
  public InputStream openStream() throws IOException {
    return new FileInputStream(file);
  }
}
