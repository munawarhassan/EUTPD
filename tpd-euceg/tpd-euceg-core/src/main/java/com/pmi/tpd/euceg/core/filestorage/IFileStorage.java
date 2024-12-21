package com.pmi.tpd.euceg.core.filestorage;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Defines a storage for creating and managing attachment files.
 *
 * @author Christophe Friederich
 * @since 1.3
 */
public interface IFileStorage {

    public static final Pattern REGEX_VALIDATION_DIRECTORY = Pattern.compile("^[\\w-]*$");

    public static final Pattern REGEX_VALIDATION_PATH = Pattern.compile("^/|[/\\w-]*$");

    /**
     * Find the file with {@code name}.
     * <p>
     * <b>Note:</b> the filename is case sensitive.
     * </p>
     *
     * @param name
     *             the file name to find.
     * @return Returns new instance of {@link IFileStorageFile} with file name {@code name} if exists, otherwise return
     *         {@code null} .
     */
    @Nullable
    IFileStorageFile findByName(@Nonnull String name);

    /**
     * Gets the file with {@code name}.
     * <p>
     * <b>Note:</b> the filename is case sensitive.
     * </p>
     *
     * @param name
     *             the file name to find.
     * @return Returns new instance of {@link IFileStorageFile} with file name {@code name}.
     * @throws FileStorageNotFoundException
     *                                      if attachment cannot be found.
     * @see IFileStorage#findByName(String)
     */
    @Nonnull
    IFileStorageFile getByName(@Nonnull String name);

    /**
     * Gets the file with unique identifier {@code uuid}.
     *
     * @param uuid
     *             the uuid to find.
     * @return Returns new instance of {@link IFileStorageFile} with unique identifier {@code uuid}.
     * @throws FileStorageNotFoundException
     *                                      if attachment cannot be found.
     */
    @Nonnull
    IFileStorageFile getByUuid(@Nonnull String uuid);

    /**
     * create and store new file with file name {@code filename}.
     *
     * @param input
     *                 a stream used.
     * @param filename
     *                 the file name of attachment
     * @return Returns new instance of {@link IFileStorageFile}
     * @throws FileStorageAlreadyExistsException
     *                                           if attachment already exists.
     * @throws IOException
     *                                           if an I/O error occurs.
     */
    @Nonnull
    IFileStorageFile createFile(@Nonnull InputStream input, @Nonnull String filename) throws IOException;

    /**
     * Update filename of file.
     *
     * @param file
     *                 the file to update
     * @param filename
     *                 the new name of attachment
     * @return
     */
    IFileStorageFile updateFilename(@Nonnull IFileStorageFile file, @Nonnull String filename);

    /**
     * Update the {@code file} with {@code input} stream.
     *
     * @param file
     *              the file to update.
     * @param input
     *              a input stream.
     * @return Returns the updated attachment.
     * @throws IOException
     *                     If an I/O error occurs reading from the input stream or writing to the file, then it may do
     *                     so after the target file has been created and after some bytes have been read or written.
     */
    IFileStorageFile replace(@Nonnull IFileStorageFile file, @Nonnull InputStream input) throws IOException;

    /**
     * Delete the {@cod file} parameter.
     *
     * @param file
     *             the file to delete
     * @return Returns {@code true} if and only if the file is successfully deleted; {@code false} otherwise.
     */
    boolean delete(@Nonnull IFileStorageFile file);

    /**
     * Gets the indicating whether a attachment with {@code filename} exists.
     *
     * @param filename
     *                 a file name.
     * @return Returns {@code true} whether a attachment with {@code filename} exists, otherwise {@code false}.
     */
    boolean exists(@Nonnull String filename);

    /**
     * Gets the indicating whether a attachment with {@code id} exists.
     *
     * @param filename
     *                 a file name.
     * @return Returns {@code true} whether a attachment with {@code id} exists, otherwise {@code false}.
     */
    boolean existsByUuid(@Nonnull String id);

    /**
     * Gets a paging list of all files and directories.
     *
     * @param pageRequest
     *                    a page request.
     * @return Returns a {@link Page} of list of all {@link IAttachment} .
     */
    @Nonnull
    Page<IFileStorageElement> findAll(@Nonnull Pageable pageRequest);

    /**
     * Gets a paging list of all files and directories contained in specific location.
     *
     * @param pageRequest
     *                           a page request.
     * @param pathSearchLocation
     *                           location to search.
     * @return Returns a {@link Page} of list of all {@link IFileStorageFile} contained in specific location.
     */
    @Nonnull
    Page<IFileStorageElement> findAll(@Nonnull final Pageable pageRequest,
        boolean getDirectory,
        @Nullable final String pathSearchLocation);

    /**
     * Gets directories for {@code directory} parameter
     *
     * @param directory
     *                  use to search
     * @return Returns the List of {@code File} representing a directory.
     */
    @Nonnull
    List<IFileStorageDirectory> getDirectories(@Nonnull String directory);

    /**
     * @param path
     * @return
     * @since 3.0
     */
    @Nonnull
    IFileStorageDirectory getDirectory(@Nonnull String path);

    /**
     * Create a directory
     *
     * @param parentPath
     *                   the relative parent path
     * @param directory
     *                   directory name
     * @throws FileStorageAlreadyExistsException
     *                                           if directory already exists
     * @since 3.0
     */
    IFileStorageDirectory createDirectory(@Nonnull final String parentPath, @Nonnull final String directory);

    /**
     * Create a directory
     *
     * @param parentPath
     *                   the relative parent path
     * @param directory
     *                   directory name
     * @throws FileStorageAlreadyExistsException
     *                                           if directory already exists
     * @since 3.0
     */
    IFileStorageDirectory createDirectory(@Nonnull final String path);

    /**
     * Delete a directory
     *
     * @param name
     *             relative path of directory
     * @since 3.0
     */
    boolean deleteDirectory(@Nonnull String name);

    /**
     * @param path
     * @return
     * @since 3.0
     */
    boolean existsDirectory(@Nonnull final String path);

    /**
     * @param directory
     * @param newName
     * @since 3.0
     */
    IFileStorageDirectory renameDirectory(IFileStorageDirectory directory, String newName);

    /**
     * @return
     * @since 3.0
     */
    ITreeDirectory walkTreeDirectories();

    /**
     * Move the {@code file} file in new location.
     *
     * @param file
     *                the file to update.
     * @param newPath
     *                the new location path
     * @return Returns the moved attachment
     * @throws IOException
     *                     if IO errors
     * @since 3.0
     */
    IFileStorageFile moveTo(@Nonnull final IFileStorageFile file, @Nonnull String newParentPath) throws IOException;

    /**
     * Move the {@code directory} directory in new location.
     *
     * @param file
     *                the file to update.
     * @param newPath
     *                the new location path
     * @return Returns the moved attachment
     * @throws IOException
     *                     if IO errors
     * @since 3.0
     */
    IFileStorageDirectory moveTo(@Nonnull IFileStorageDirectory directory, @Nonnull String newParentPath)
            throws IOException;

}
