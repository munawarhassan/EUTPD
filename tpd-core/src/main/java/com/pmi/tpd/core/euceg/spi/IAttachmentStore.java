package com.pmi.tpd.core.euceg.spi;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.pmi.tpd.api.paging.IFilterable;
import com.pmi.tpd.core.euceg.AttachmentInvalidFilenaneException;
import com.pmi.tpd.core.euceg.ConcurrencyAttachmentAccessException;
import com.pmi.tpd.core.model.euceg.AttachmentEntity;
import com.pmi.tpd.core.model.euceg.AttachmentRevision;
import com.pmi.tpd.euceg.api.entity.AttachmentSendStatus;
import com.pmi.tpd.euceg.api.entity.ISubmitterEntity;

/**
 * @author Christophe Friederich
 * @since 1.0
 */
public interface IAttachmentStore {

    /**
     * @return Returns the number of stored attachments.
     * @since 2.2
     */
    long count();

    /**
     * @param filename
     * @return
     * @since 3.4
     */
    long getCountByFilename(final String filename);

    /**
     * @param uuid
     *             the unique identifier corresponding to attachment.
     * @return Returns the {@link AttachmentEntity} associated to the {@code uuid}.
     */
    Optional<AttachmentEntity> findOne(@Nonnull String uuid);

    /**
     * @param uuid
     *             the unique identifier corresponding to attachment.
     * @return Returns the {@link AttachmentEntity} associated to the {@code uuid}.
     */
    AttachmentEntity get(@Nonnull String uuid);

    /**
     * @param uuid
     *             the unique identifier corresponding to attachment.
     */
    void delete(@Nonnull String uuid);

    /**
     * @param filename
     *                 the file name corresponding to attachment.
     * @return Returns the {@link AttachmentEntity} associated to the {@code filename}.
     */
    AttachmentEntity getByFilename(@Nonnull String filename);

    /**
     * @param filename
     *                 the file name corresponding to attachment.
     * @return Returns the {@link AttachmentEntity} associated to the {@code filename} as a unique result or
     *         {@code null} if no result is found.
     */
    @Nonnull
    Optional<AttachmentEntity> findByFilename(@Nullable String filename);

    /**
     * Retrieves a page of attachment.
     * <p>
     * Note: if use {@link IFilterable} To filter, the {@link IFilterable#getQuery() predicate} allows to retrieve
     * attachment containing ignoring case the filename.
     * </p>
     *
     * @param pageRequest
     *                    defines the page of groups to retrieve
     * @return Returns the requested page of attachments, which may be empty but never {@code null}
     */
    @Nonnull
    Page<AttachmentEntity> findAll(@Nonnull Pageable pageRequest);

    /**
     * @param predicate
     * @return
     */
    @Nonnull
    Iterable<AttachmentEntity> findAllByFilename(@Nonnull String filename);

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
     * @since 2.4
     */
    @Nonnull
    AttachmentRevision getCurrentRevision(String uuid);

    /**
     * @param searchTerm
     *                   the term to search
     * @return Returns a list of all {@link AttachmentEntity} with filename containing {@code searchTerm} ignoring case.
     */
    @Nonnull
    List<AttachmentEntity> searchByFileName(@Nullable String searchTerm);

    /**
     * @param filename
     *                 a exact filename to check.
     * @param excludes
     *                 entity to exclude.
     * @return Returns {@code true} whether the filename exists, otherwise {@code false}.
     */
    boolean exists(@Nullable String filename, AttachmentEntity... excludes);

    /**
     * @param attachment
     *                   an attachment.
     * @return Returns the saved attachment entity.
     */
    AttachmentEntity create(@Nonnull AttachmentEntity attachment) throws AttachmentInvalidFilenaneException;

    /**
     * @param attachment
     *                   an attachment.
     * @return Returns the updated attachment entity.
     * @throws ConcurrencyAttachmentAccessException
     *                                              occurs if the attachment is sending.
     */
    AttachmentEntity update(@Nonnull AttachmentEntity attachment)
            throws ConcurrencyAttachmentAccessException, AttachmentInvalidFilenaneException;

    /**
     * @param attachment
     *                   an attachment.
     * @param submitter
     *                   a submitter associate to
     * @param status
     *                   the status to use
     * @return Returns a {@link AttachmentEntity} updated with new {@link AttachmentSendStatus status}.
     */
    @Nonnull
    AttachmentEntity updateSendStatus(@Nonnull AttachmentEntity attachment,
        @Nonnull ISubmitterEntity submitter,
        @Nonnull AttachmentSendStatus status);

    /**
     * Sets the attachment as Updated for all submitters if it has already sent.
     *
     * @param attachment
     *                   an attachment.
     * @return Returns new persisted instance of attachment.
     * @throws ConcurrencyAttachmentAccessException
     *                                              occurs if the attachment is sending.
     */
    AttachmentEntity updateAttachmentAsUpdated(@Nonnull AttachmentEntity attachment)
            throws ConcurrencyAttachmentAccessException;

}
