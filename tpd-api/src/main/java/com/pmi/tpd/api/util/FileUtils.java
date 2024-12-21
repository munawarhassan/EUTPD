package com.pmi.tpd.api.util;

import static com.pmi.tpd.api.util.Assert.checkNotNull;
import static com.pmi.tpd.api.util.Assert.state;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Joiner;
import com.pmi.tpd.api.util.zip.UrlUnzipper;

/**
 * @author Christophe Friederich
 * @since 1.3
 */
public final class FileUtils {

  /** */
  private static final Logger LOGGER = LoggerFactory.getLogger(FileUtils.class);

  private FileUtils() {
    throw new UnsupportedOperationException(
        getClass().getName() + " is a utility class and should not be instantiated");
  }

  /**
   * Extract the zip from the URL into the destination directory, but only if the
   * contents haven't already been
   * unzipped. If the directory contains different contents than the zip, the
   * directory is cleaned out and the files
   * are unzipped.
   *
   * @param zipUrl
   *                The zip url
   * @param destDir
   *                The destination directory for the zip contents
   */
  public static void conditionallyExtractZipFile(final URL zipUrl, final File destDir) {
    try {
      final UrlUnzipper unzipper = new UrlUnzipper(zipUrl, destDir);
      unzipper.conditionalUnzip();
    } catch (final IOException e) {
      LOGGER.error("Found " + zipUrl + ", but failed to read file", e);
    }
  }

  /**
   * <p>
   * delete Directory.
   * </p>
   *
   * @param pathToBeDeleted
   *                        a {@link java.io.Path} object.
   * @throws IOException
   */
  public static void deleteDirectory(final Path pathToBeDeleted) {
    try {
      Files.walk(pathToBeDeleted).sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
    } catch (final IOException e) {
      throw new RuntimeException(e.getLocalizedMessage(), e);
    }
  }

  /**
   * Constructs a file path from a series of path segments.
   *
   * @param base
   *                 base directory
   * @param segments
   *                 subsequent path segments
   * @return a {@link File} with the appropriate file separator for the OS
   */
  @Nonnull
  public static File construct(@Nonnull final File base, final String... segments) {
    return segments.length == 0 ? base : new File(base, join(segments));
  }

  /**
   * Uses Java's {@code File.createTempFile} to create a temporary directory.
   * <p>
   * Since Java provides no mechanism for creating a temporary directory, a temp
   * file is created (to get a name), and
   * then deleted and a directory is made in the same location.
   *
   * @param prefix
   *                  the prefix string to be used in generating the directory's
   *                  name; must be at least three characters
   *                  long
   * @param suffix
   *                  the suffix string to be used in generating the directory's
   *                  name; may be {@code null}, in which case
   *                  the suffix {@code ".tmp"} will be used
   * @param directory
   *                  the directory in which the directory is to be created, or
   *                  {@code null} if the default temporary file
   *                  directory should be used
   * @return A newly-created empty directory
   * @throws IOException
   *                     If a directory could not be created
   */
  @Nonnull
  public static File createTempDir(@Nonnull final String prefix,
      @Nullable final String suffix,
      @Nullable final File directory) throws IOException {
    final File temporary = File.createTempFile(prefix, suffix, directory);
    if (temporary.delete() && temporary.mkdir()) {
      return temporary;
    }
    throw new IOException("Could not create directory: " + temporary);
  }

  /**
   * Joins the provided path {@code segments} using the path separator for the
   * underlying OS.
   *
   * @param segments
   *                 the path segments to join
   * @return a {@code String} containing the provided {@code segments}, joined
   *         with the correct OS-specific separator
   * @since 2.4
   */
  @Nonnull
  public static String join(final String... segments) {
    return Joiner.on(File.separator).join(segments);
  }

  /**
   * Creates the specified {@code directory}, if it does not already exist. If the
   * path does exist, it is validated
   * that it is a directory and not a file.
   *
   * @param directory
   *                  the directory to create
   * @throws IllegalStateException
   *                               if the {@code directory} path already exists
   *                               and is not a directory, or if the directory
   *                               cannot be
   *                               created
   * @throws NullPointerException
   *                               if the provided {@code directory} is
   *                               {@code null}
   */
  public static void mkdir(@Nonnull final File directory) {
    checkNotNull(directory, "directory");

    if (directory.exists()) {
      if (!directory.isDirectory()) {
        throw new IllegalStateException(directory.getAbsolutePath() + " already exists and is not a directory");
      }
    } else if (!directory.mkdirs()) {
      throw new IllegalStateException("Could not create " + directory.getAbsolutePath());
    }
  }

  /**
   * Creates a directory with the specified {@code path}, if it does not already
   * exist. If the path does exist, it is
   * validated that it is a directory and not a file.
   *
   * @param path
   *             the path for the new directory
   * @return the created directory
   * @throws IllegalArgumentException
   *                                  if the {@code path} is blank or empty
   * @throws IllegalStateException
   *                                  if the specified {@code path} already exists
   *                                  and is not a directory, or if the directory
   *                                  cannot be
   *                                  created
   * @throws NullPointerException
   *                                  if the provided {@code path} is {@code null}
   */
  @Nonnull
  public static File mkdir(@Nonnull final String path) {
    state(checkNotNull(path, "path").trim().isEmpty(), "A path for the created directory is required");

    final File dir = new File(path);
    mkdir(dir);

    return dir;
  }

  /**
   * Creates the specified {@code child} directory beneath the {@code parent}, if
   * it does not already exist. If the
   * path does exist, it is validated that it is a directory and not a file. If no
   * {@code parent} is provided, the
   * {@code child} will be created in the working directory for the application.
   *
   * @param parent
   *               the base path for creating the new directory, or {@code null}
   *               to use the working directory
   * @param child
   *               the path beneath the parent for the new directory
   * @return the created directory
   * @throws IllegalArgumentException
   *                                  if the {@code child} path is blank or empty
   * @throws IllegalStateException
   *                                  if the {@code child} path already exists and
   *                                  is not a directory, or if the directory
   *                                  cannot be
   *                                  created
   * @throws NullPointerException
   *                                  if the provided {@code child} is
   *                                  {@code null}
   */
  @Nonnull
  public static File mkdir(@Nullable final File parent, @Nonnull final String child) {
    state(!checkNotNull(child, "child").trim().isEmpty(), "A path for the created directory is required");

    final File dir = new File(parent, child);
    mkdir(dir);

    return dir;
  }
}
