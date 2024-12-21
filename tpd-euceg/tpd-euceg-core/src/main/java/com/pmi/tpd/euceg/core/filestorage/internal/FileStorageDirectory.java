package com.pmi.tpd.euceg.core.filestorage.internal;

import static com.pmi.tpd.api.util.Assert.checkNotNull;
import static com.pmi.tpd.api.util.Assert.state;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.annotation.Nonnull;

import com.pmi.tpd.euceg.core.filestorage.IFileStorageDirectory;

/**
 * Default implementation of interface {@link IFileStorageDirectory}.
 *
 * @author friederich christophe
 * @since 3.0
 */
public class FileStorageDirectory implements IFileStorageDirectory {

  /** */
  private final String name;

  /** */
  private final Path path;

  /** */
  private final Path relativeParentPath;

  /**
   * Create {@link FileStorageDirectory} for the associated to {@code path}.
   *
   * @param path
   *                   the path of a directory.
   * @param parentPath
   *                   the parent path relative to root physical path.
   */
  public FileStorageDirectory(@Nonnull final Path path, @Nonnull final Path parentPath) {
    state(!checkNotNull(parentPath, "parentPath").isAbsolute(), "should be relative");
    this.path = checkNotNull(path, "path");
    this.name = path.getFileName().toString();
    this.relativeParentPath = parentPath;
  }

  /** {@inheritDoc} */
  @Override
  public boolean isDirectory() {
    return true;
  }

  /** {@inheritDoc} */
  @Override
  public boolean isEmpty() {
    try (DirectoryStream<Path> directory = Files.newDirectoryStream(path)) {
      return !directory.iterator().hasNext();
    } catch (final IOException e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }

  /** {@inheritDoc} */
  @Override
  @Nonnull
  public String getName() {
    return name;
  }

  /** {@inheritDoc} */
  @Override
  @Nonnull
  public File getFile() {
    return path.toFile();
  }

  /** {@inheritDoc} */
  @Override
  @Nonnull
  public Path getRelativeParentPath() {
    return relativeParentPath;
  }

  /** {@inheritDoc} */
  @Override
  public Path getRelativePath() {
    return relativeParentPath.resolve(getName());
  }

}
