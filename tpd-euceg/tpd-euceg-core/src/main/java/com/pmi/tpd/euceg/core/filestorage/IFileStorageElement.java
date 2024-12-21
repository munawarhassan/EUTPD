package com.pmi.tpd.euceg.core.filestorage;

import java.io.File;
import java.nio.file.Path;

import javax.annotation.Nonnull;

/**
 * This interface represents the common base of elements provide by
 * {@link IFileStorage}.
 *
 * @author christophe friederich
 * @see IFileStorageFile, IFileStorageDirectory
 * @since 3.0
 */
public interface IFileStorageElement {

  /**
   * Gets the indicating whether this element is a directory.
   *
   * @return Returns {@code true} whether this element is a directory, otherwise
   *         {@code false}.
   */
  boolean isDirectory();

  /**
   * Gets the name.
   *
   * @return Returns a {@link String} representing the name of this element.
   */
  @Nonnull
  String getName();

  /**
   * @return Returns a {@link File} representing this element.
   */
  File getFile();

  /**
   * Gets the relative path.
   *
   * @return Returns a {@link Path} representing the path relative location to
   *         physical root location.
   */
  @Nonnull
  Path getRelativePath();

  /**
   * Gets the relative parent path
   *
   * @return Returns a {@link Path} representing the parent relative path to
   *         physical root location.
   */
  @Nonnull
  public Path getRelativeParentPath();

}
