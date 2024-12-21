package com.pmi.tpd.core.euceg.impl;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.eu.ceg.AttachmentAction;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.pmi.tpd.api.paging.IFilterable;
import com.pmi.tpd.api.util.Assert;
import com.pmi.tpd.core.euceg.AttachmentInvalidFilenaneException;
import com.pmi.tpd.core.euceg.ConcurrencyAttachmentAccessException;
import com.pmi.tpd.core.euceg.spi.IAttachmentRepository;
import com.pmi.tpd.core.euceg.spi.IAttachmentStore;
import com.pmi.tpd.core.model.euceg.AttachmentEntity;
import com.pmi.tpd.core.model.euceg.AttachmentRevision;
import com.pmi.tpd.core.model.euceg.QAttachmentEntity;
import com.pmi.tpd.core.model.euceg.StatusAttachment;
import com.pmi.tpd.core.model.euceg.StatusAttachment.StatusAttachmentId;
import com.pmi.tpd.database.hibernate.HibernateUtils;
import com.pmi.tpd.euceg.api.entity.AttachmentSendStatus;
import com.pmi.tpd.euceg.api.entity.IStatusAttachment;
import com.pmi.tpd.euceg.api.entity.ISubmitterEntity;
import com.querydsl.core.types.Predicate;

/**
 * @author Christophe Friederich
 * @since 1.0
 */
@Singleton
@Transactional(propagation = Propagation.REQUIRED, readOnly = true)
public class AttachmentStore implements IAttachmentStore {

    private static final Pattern FILENAME_PATTERN = Pattern.compile("^[\\(\\)\\.,\\-\\w ]*$");

    /** */
    private static final Logger LOGGER = LoggerFactory.getLogger(AttachmentStore.class);

    /** */
    private final IAttachmentRepository attachmentRepository;

    private QAttachmentEntity entity() {
        return this.attachmentRepository.entity();
    }

    /**
     * Default constructor.
     *
     * @param attachmentRepository
     *                             associated repository.
     */
    @Inject
    public AttachmentStore(@Nonnull final IAttachmentRepository attachmentRepository) {
        this.attachmentRepository = Assert.checkNotNull(attachmentRepository, "attachmentRepository");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long count() {
        return this.attachmentRepository.count();
    }

    @Override
    public long getCountByFilename(final String filename) {
        return this.attachmentRepository.count(entity().filename.eq(filename));

    }

    @Override
    public Optional<AttachmentEntity> findOne(@Nonnull final String uuid) {
        return this.attachmentRepository.findOne(entity().attachmentId.eq(Assert.checkHasText(uuid, "uuid")))
                .map(att -> HibernateUtils.initialize(att));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AttachmentEntity get(@Nonnull final String uuid) {
        return HibernateUtils.initialize(this.attachmentRepository.getById(Assert.checkHasText(uuid, "uuid")));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void delete(@Nonnull final String uuid) {
        this.attachmentRepository.deleteById(Assert.checkHasText(uuid, "uuid"));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AttachmentEntity getByFilename(final @Nonnull String filename) {
        final AttachmentEntity entity = this.attachmentRepository.getByFilename(filename);

        return entity;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nonnull
    public Optional<AttachmentEntity> findByFilename(final @Nullable String filename) {
        return this.attachmentRepository.findByFilename(filename);
    }

    @Override
    @Nonnull
    public Iterable<AttachmentEntity> findAllByFilename(final @Nonnull String filename) {
        return this.attachmentRepository.findAll(entity().filename.eq(filename), entity().createdDate.desc());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nonnull
    public Page<AttachmentEntity> findAll(final @Nonnull Pageable pageRequest) {
        Predicate predicate = null;
        if (pageRequest instanceof IFilterable) {
            if (!Strings.isNullOrEmpty(((IFilterable) pageRequest).getQuery())) {
                predicate = this.attachmentRepository.entity().filename
                        .containsIgnoreCase(((IFilterable) pageRequest).getQuery());
            }
        }
        return this.attachmentRepository.findAll(predicate, pageRequest);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nonnull
    public Page<AttachmentRevision> findRevisions(@Nonnull final String uuid, @Nonnull final Pageable pageRequest) {

        return this.attachmentRepository.findRevisions(uuid, pageRequest).map(AttachmentRevision::fromRevision);
    }

    @Override
    @Nonnull
    public AttachmentRevision getCurrentRevision(final String uuid) {
        return this.attachmentRepository.findLastChangeRevision(uuid)
                .map(AttachmentRevision::fromRevision)
                .orElseThrow();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nonnull
    public List<AttachmentEntity> searchByFileName(final @Nullable String filename) {
        if (Strings.isNullOrEmpty(filename)) {
            return Collections.emptyList();
        }
        return Lists.newArrayList(
            this.attachmentRepository.findAll(entity().filename.containsIgnoreCase(filename), entity().filename.asc()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean exists(final @Nullable String filename, @Nullable final AttachmentEntity... excludes) {
        Assert.checkHasText(filename, "filename");
        if (excludes == null || excludes.length == 0) {
            return this.attachmentRepository.exists(entity().filename.eq(filename));
        }
        return this.attachmentRepository.exists(entity().filename.eq(filename).and(entity().notIn(excludes)));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public AttachmentEntity create(@Nonnull final AttachmentEntity attachment)
            throws AttachmentInvalidFilenaneException {
        Assert.checkNotNull(attachment, "attachment");
        checkFilenameValidation(attachment);
        return attachmentRepository.save(attachment);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public AttachmentEntity update(final @Nonnull AttachmentEntity attachment)
            throws ConcurrencyAttachmentAccessException, AttachmentInvalidFilenaneException {
        checkConcurrencyAccess(attachment);
        checkFilenameValidation(attachment);
        return this.attachmentRepository.saveAndFlush(attachment);
    }

    @Override
    @Transactional
    public @Nonnull AttachmentEntity updateSendStatus(final @Nonnull AttachmentEntity attachment,
        final @Nonnull ISubmitterEntity submitter,
        final @Nonnull AttachmentSendStatus sendStatus) {
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("updateSendStatus -> Update send status of attachment: {}, send status: {}",
                attachment.getFilename(),
                sendStatus);
        }
        return this.attachmentRepository.saveAndFlush(attachment.copy()
                .status(attachment.getStatus(submitter)
                        .map(status -> status.copy().sendStatus(sendStatus).build())
                        .orElseGet(() -> StatusAttachment.builder()
                                .id(StatusAttachmentId.key(attachment.getId(), submitter.getId()))
                                .sendStatus(sendStatus)
                                .action(AttachmentAction.CREATE)
                                .build()))
                // TODO add test validate update of attachment and the index
                // enforce update attachment to synchronize of index
                .lastModifiedDate(DateTime.now())
                .build());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public AttachmentEntity updateAttachmentAsUpdated(@Nonnull final AttachmentEntity attachment)
            throws ConcurrencyAttachmentAccessException {
        checkConcurrencyAccess(attachment);
        final AttachmentEntity.Builder builder = attachment.copy();
        for (final IStatusAttachment status : attachment.getStatus()) {
            // the document has been already sent.

            if (status.isSent()) {
                // remove before to add
                builder.status().remove(status);
                builder.status(status.copy().action(AttachmentAction.UPDATE).noSend().build());
            }
        }
        return this.attachmentRepository.save(builder.build());
    }

    private AttachmentEntity checkConcurrencyAccess(final AttachmentEntity attachment)
            throws ConcurrencyAttachmentAccessException {
        for (final IStatusAttachment status : attachment.getStatus()) {
            // can not update file then it is sending.
            if (AttachmentSendStatus.SENDING.equals(status.getSendStatus())) {
                if (LOGGER.isInfoEnabled()) {
                    LOGGER.info("Concurrency Attachment Access : {}, send status: {}", attachment.getFilename());
                }
                throw new ConcurrencyAttachmentAccessException(
                        "The filename " + attachment.getFilename() + "cannot be updated, then it is sending.");
            }
        }
        return attachment;
    }

    private void checkFilenameValidation(final AttachmentEntity attachment) throws AttachmentInvalidFilenaneException {
        if (!FILENAME_PATTERN.matcher(attachment.getFilename()).find()) {
            throw new AttachmentInvalidFilenaneException("The filename " + attachment.getFilename()
                    + " is invalid. Only word character, dot, minus, comma, parenthesis, underscore and space are accepted.");
        }

    }

}
