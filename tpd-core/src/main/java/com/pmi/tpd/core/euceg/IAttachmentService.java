package com.pmi.tpd.core.euceg;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.NoSuchElementException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.eu.ceg.Attachment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.pmi.tpd.core.euceg.filestorage.DirectoryUpateRequest;
import com.pmi.tpd.core.euceg.filestorage.FsDirectory;
import com.pmi.tpd.core.euceg.filestorage.FsFile;
import com.pmi.tpd.core.euceg.filestorage.FsTreeDirectory;
import com.pmi.tpd.core.euceg.filestorage.IFsElement;
import com.pmi.tpd.core.euceg.filestorage.MoveDirectoryRequest;
import com.pmi.tpd.core.euceg.filestorage.MoveFileRequest;
import com.pmi.tpd.core.model.euceg.AttachmentRevision;
import com.pmi.tpd.euceg.api.entity.AttachmentSendStatus;
import com.pmi.tpd.euceg.api.entity.IAttachmentEntity;
import com.pmi.tpd.euceg.api.entity.ISubmitterEntity;
import com.pmi.tpd.euceg.core.filestorage.FileStorageAlreadyExistsException;
import com.pmi.tpd.euceg.core.filestorage.FileStorageNotFoundException;
import com.pmi.tpd.euceg.core.filestorage.IFileStorage;
import com.pmi.tpd.euceg.core.filestorage.IFileStorageFile;

/**
 * @author christophe friederich
 * @since 3.0
 */
public interface IAttachmentService {

    /**
     * @param filename
     * @param submitter
     * @return
     */
    @Nonnull
    Attachment createAttachment(@Nonnull String filename, @Nonnull ISubmitterEntity submitter);

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
    Page<IFsElement<AttachmentRequest>> findAll(@Nonnull final Pageable pageRequest,
        @Nullable final String pathSearchLocation);

    /**
     * @param searchTerm
     *                   the term to search
     * @return Returns a list of all {@link IAttachmentEntity} with filename containing {@code searchTerm} ignoring
     *         case.
     */

    List<? extends IAttachmentEntity> searchByFileName(@Nullable String searchTerm);

    /**
     * @param filename
     *                 a exact filename to check.
     * @param excludes
     *                 entity to exclude.
     * @return Returns {@code true} whether the filename exists, otherwise {@code false}.
     */
    boolean exists(@Nonnull String filename, @Nullable IAttachmentEntity... excludes);

    /**
     * Store the attachment file in {@link IFileStorage file storage}.
     * <p>
     * The attachment is replaced if already exists or created.
     * </p>
     *
     * @return Returns updated entity attachment.
     * @param in
     *                     input stream containing the file to store.
     * @param filename
     *                     the file name
     * @param contentType
     *                     the type of file.
     * @param confidential
     *                     indicating if the attachment is confidential.
     * @throws IOException
     *                                              if an I/O error occurs.n
     * @throws ConcurrencyAttachmentAccessException
     *                                              occurs if the attachment is sending.
     */
    @Nonnull
    IAttachmentEntity storeAttachment(@Nonnull InputStream in,
        @Nonnull String filename,
        @Nonnull String contentType,
        boolean confidential)
            throws IOException, ConcurrencyAttachmentAccessException, AttachmentInvalidFilenaneException;

    /**
     * @param attachment
     *                   to update
     * @return Returns a updated attachment.
     * @throws ConcurrencyAttachmentAccessException
     *                                              occurs if the attachment is sending.
     */
    @Nonnull
    IAttachmentEntity updateAttachment(@Nonnull AttachmentUpdate attachment)
            throws ConcurrencyAttachmentAccessException, AttachmentInvalidFilenaneException;

    /**
     * Gets the {@link IAttachment attachment} coressponding to {@code uuid}.
     *
     * @param uuid
     *             the uuid of attachment to get
     * @return Returns a instance of {@link IAttachment} representing the attachment associated to {@code uuid}.
     * @throws FileStorageNotFoundException
     *                                      if attachment cannot be found.
     */
    @Nonnull
    IAttachmentEntity getAttachment(@Nonnull String uuid);

    /**
     * @param filename
     *                 the file name corresponding to attachment.
     * @return Returns the {@link IAttachmentEntity} associated to the {@code filename}.
     */
    @Nonnull
    IAttachmentEntity getAttachmentByFilename(@Nonnull String filename);

    /**
     * @param uuid
     * @return
     */
    @Nonnull
    DownloadableAttachment getDownloadableAttachment(String uuid);

    /**
     * @return
     */
    @Nonnull
    FsTreeDirectory getTreeDirectory();

    /**
     * Create a directory
     *
     * @param path
     *             relative path of new directory
     * @throws FileStorageAlreadyExistsException
     *                                           if directory already exists
     */
    FsDirectory<?> createDirectory(@Nonnull String path);

    /**
     * Create a directory
     *
     * @param parent
     *               the relative parent path
     * @param name
     *               the name of directory
     * @throws FileStorageAlreadyExistsException
     *                                           if directory already exists
     */
    FsDirectory<?> createDirectory(@Nonnull FsDirectory<?> parent, @Nonnull String name);

    /**
     * Remove the attachment corresponding to {@code uuid}.
     *
     * @param uuid
     *             a uuid of attachment to remove.
     */
    void deleteAttachment(@Nonnull String uuid);

    /**
     * @param directory
     * @param request
     */
    FsDirectory<?> renameDirectory(@Nonnull String directory, @Nonnull DirectoryUpateRequest request);

    /**
     * @param request
     * @return
     * @throws IOException
     */
    FsDirectory<?> moveDirectoryTo(final MoveDirectoryRequest request) throws IOException;

    /**
     * @param request
     * @return
     * @throws IOException
     */
    FsFile<AttachmentRequest> moveFileTo(final MoveFileRequest request) throws IOException;

    /**
     * Delete a directory
     *
     * @return Return {@code true} if and only if the file or directory is successfully deleted; {@code false} otherwise
     * @param name
     *             relative path of directory
     */
    boolean deleteDirectory(@Nonnull String name);

    /**
     * @param attachment
     *                   an attachment.
     * @param submitter
     *                   a submitter associate to
     * @param status
     *                   the status to use
     * @return Returns a {@link IAttachmentEntity} updated with new {@link AttachmentSendStatus status}.
     */
    IAttachmentEntity updateSendStatus(@Nonnull IAttachmentEntity attachment,
        @Nonnull ISubmitterEntity submitter,
        @Nonnull AttachmentSendStatus status);

    /**
     * Retrieve a page of attachment revisions
     *
     * @param uuid
     *                    attachment UUID
     * @param pageRequest
     *                    the page
     * @return the requested page of attachments, which may be empty but never {@code null}
     * @since 2.4
     */
    @Nonnull
    Page<AttachmentRevision> findRevisions(@Nonnull String uuid, @Nonnull Pageable pageRequest);

    /**
     * Gets the current revision of specific attachment.
     *
     * @param uuid
     *             attachment UUID
     * @return Returns the current attachment revision associated to.
     * @throws NoSuchElementException
     *                                if no value is present
     */
    @Nonnull
    AttachmentRevision getCurrentRevision(String uuid);

    /**
     * @param filename
     */
    void checkIntegrity(@Nonnull String filename);

    /**
     * @param filename
     */
    void fixIntegrity(@Nonnull String filename);

}
