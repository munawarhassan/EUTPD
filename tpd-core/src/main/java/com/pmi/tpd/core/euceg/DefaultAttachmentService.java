package com.pmi.tpd.core.euceg;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.eu.ceg.Attachment;
import org.glassfish.jersey.internal.guava.Lists;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.pmi.tpd.api.event.publisher.IEventPublisher;
import com.pmi.tpd.api.i18n.I18nService;
import com.pmi.tpd.api.util.Assert;
import com.pmi.tpd.core.euceg.event.AttachmentActionEvent;
import com.pmi.tpd.core.euceg.event.AttachmentMovedEvent;
import com.pmi.tpd.core.euceg.filestorage.DirectoryUpateRequest;
import com.pmi.tpd.core.euceg.filestorage.FsDirectory;
import com.pmi.tpd.core.euceg.filestorage.FsFile;
import com.pmi.tpd.core.euceg.filestorage.FsTreeDirectory;
import com.pmi.tpd.core.euceg.filestorage.IFsElement;
import com.pmi.tpd.core.euceg.filestorage.MoveDirectoryRequest;
import com.pmi.tpd.core.euceg.filestorage.MoveFileRequest;
import com.pmi.tpd.core.euceg.spi.IAttachmentStore;
import com.pmi.tpd.core.model.euceg.AttachmentEntity;
import com.pmi.tpd.core.model.euceg.AttachmentRevision;
import com.pmi.tpd.euceg.api.EucegException;
import com.pmi.tpd.euceg.api.entity.AttachmentSendStatus;
import com.pmi.tpd.euceg.api.entity.IAttachmentEntity;
import com.pmi.tpd.euceg.api.entity.IStatusAttachment;
import com.pmi.tpd.euceg.api.entity.ISubmitterEntity;
import com.pmi.tpd.euceg.core.filestorage.IFileStorage;
import com.pmi.tpd.euceg.core.filestorage.IFileStorageDirectory;
import com.pmi.tpd.euceg.core.filestorage.IFileStorageFile;
import com.pmi.tpd.security.annotation.Unsecured;
import com.querydsl.core.NonUniqueResultException;

@Singleton
@Transactional(propagation = Propagation.REQUIRED, readOnly = true)
public class DefaultAttachmentService implements IAttachmentService {

    /** */
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultAttachmentService.class);

    /** */
    private final IAttachmentStore attachmentStore;

    /** */
    private final IFileStorage fileStorage;

    /** */
    private final IEventPublisher publisher;

    /** */
    private final I18nService i18nService;

    @Inject
    public DefaultAttachmentService(@Nonnull final IAttachmentStore attachmentStore,
            @Nonnull final IFileStorage fileStorage, @Nonnull final IEventPublisher publisher,
            @Nonnull final I18nService i18nService) {
        this.attachmentStore = Assert.checkNotNull(attachmentStore, "attachmentStore");
        this.fileStorage = Assert.checkNotNull(fileStorage, "fileStorage");
        this.publisher = Assert.checkNotNull(publisher, "publisher");
        this.i18nService = Assert.checkNotNull(i18nService, "i18nService");
    }

    @Unsecured("used internally in DefaultSenderMessageHandler")
    @Nonnull
    @Override
    public Attachment createAttachment(@Nonnull final String filename, @Nonnull final ISubmitterEntity submitter) {
        final AttachmentEntity attachmentEntity = this.attachmentStore.getByFilename(filename);
        final IFileStorageFile fileAttachment = fileStorage.getByUuid(attachmentEntity.getId());

        return attachmentEntity.toAttachment(submitter, fileAttachment.getFile());
    }

    /**
     * {@inheritDoc}
     */
    @PreAuthorize("hasGlobalPermission('USER')")
    @Override
    @Nonnull
    public Page<IFsElement<AttachmentRequest>> findAll(final @Nonnull Pageable pageRequest,
        final @Nullable String pathSearchLocation) {
        return this.fileStorage.findAll(pageRequest, true, pathSearchLocation)
                .map(item -> IFsElement.create(item,
                    !item.isDirectory() ? attachmentStore.findOne(((IFileStorageFile) item).getUUID())
                            .map(AttachmentRequest::from)
                            .orElse(null) : null)
                        .orElseThrow());
    }

    /**
     * {@inheritDoc}
     */
    @PreAuthorize("hasGlobalPermission('USER')")
    @Override
    @Nonnull
    public List<? extends IAttachmentEntity> searchByFileName(final @Nullable String searchTerm) {
        return this.attachmentStore.searchByFileName(searchTerm);
    }

    /**
     * {@inheritDoc}
     */
    @PreAuthorize("hasGlobalPermission('USER')")
    @Override
    public boolean exists(@Nonnull final String filename, @Nullable final IAttachmentEntity... excludes) {
        return this.attachmentStore.exists(filename, Arrays.stream(excludes).toArray(AttachmentEntity[]::new));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @PreAuthorize("hasGlobalPermission('USER')")
    @Transactional(propagation = Propagation.REQUIRED,
            rollbackFor = { RuntimeException.class, ConcurrencyAttachmentAccessException.class, IOException.class })
    @Nonnull
    public AttachmentEntity storeAttachment(@Nonnull final InputStream in,
        @Nonnull final String filename,
        @Nonnull final String contentType,
        final boolean confidential)
            throws IOException, ConcurrencyAttachmentAccessException, AttachmentInvalidFilenaneException {
        Assert.checkHasText(filename, "filename");
        Assert.checkHasText(contentType, "contentType");
        Assert.checkNotNull(in, "in");
        if (attachmentStore.exists(filename)) {
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("Store file: Replace file '{}'", filename);
            }
            IFileStorageFile attachment = fileStorage.getByName(filename);
            AttachmentEntity attachmentEntity = attachmentStore.getByFilename(filename);
            attachment = fileStorage.replace(attachment, in);
            // enforce the update audit trail.
            attachmentEntity = attachmentStore
                    .updateAttachmentAsUpdated(attachmentEntity.copy().lastModifiedDate(DateTime.now()).build());
            this.publisher.publish(createActionEvent(AttachmentActionEvent.AttachmentAction.updated, attachment));
            return attachmentEntity;
        } else {
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("Store file: Create and store file '{}'", filename);
            }
            final IFileStorageFile attachment = fileStorage.createFile(in, filename);
            try {
                final AttachmentEntity attachmentEntity = attachmentStore.create(AttachmentEntity.builder()
                        .filename(filename)
                        .attachmentId(attachment.getUUID())
                        .confidential(confidential)
                        .contentType(contentType)
                        .build());
                this.publisher.publish(createActionEvent(AttachmentActionEvent.AttachmentAction.created, attachment));
                return attachmentEntity;
            } catch (final Exception ex) {
                // try to remove newly attachment
                fileStorage.delete(attachment);
                throw ex;
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @PreAuthorize("hasGlobalPermission('ADMIN')")
    @Transactional(propagation = Propagation.REQUIRED,
            rollbackFor = { RuntimeException.class, ConcurrencyAttachmentAccessException.class })
    @Nonnull
    public AttachmentEntity updateAttachment(@Nonnull final AttachmentUpdate attachment)
            throws ConcurrencyAttachmentAccessException, AttachmentInvalidFilenaneException {
        Assert.checkNotNull(attachment, "attachment");
        AttachmentEntity entity = this.attachmentStore.get(attachment.getAttachmentId());
        if (this.attachmentStore.exists(attachment.getFilename(), entity)) {
            throw new EucegException(
                    i18nService.createKeyedMessage("app.service.euceg.attachment.filename.alreadyexist",
                        attachment.getFilename()));
        }
        IFileStorageFile fsFile = this.fileStorage.getByUuid(attachment.getAttachmentId());
        fsFile = this.fileStorage.updateFilename(fsFile, attachment.getFilename());

        // can not change confidentiality and status in same time.
        if (entity.isConfidential() != attachment.isConfidential()) {
            // change only confidentiality flag
            entity = this.attachmentStore.updateAttachmentAsUpdated(
                entity.copy().filename(attachment.getFilename()).confidential(attachment.isConfidential()).build());
        } else {
            final AttachmentEntity.Builder builder = entity.copy().filename(attachment.getFilename());
            if (attachment.getAction() != null && attachment.getSendStatus() != null) {

                final Optional<IStatusAttachment> status = entity.getDefaultStatus();
                if (status.isPresent()) {
                    final IStatusAttachment astatus = status.get();
                    builder.status(
                        astatus.copy().action(attachment.getAction()).sendStatus(attachment.getSendStatus()).build())
                            .build();
                }
            }
            entity = this.attachmentStore.update(builder.lastModifiedDate(DateTime.now()).build());
        }
        return entity;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @PreAuthorize("hasGlobalPermission('USER')")
    @Transactional
    public void deleteAttachment(@Nonnull final String uuid) {
        if (fileStorage.existsByUuid(uuid)) {
            final IFileStorageFile attachment = fileStorage.getByUuid(uuid);
            if (this.attachmentStore.exists(attachment.getName())) {
                this.attachmentStore.delete(uuid);
            }
            this.fileStorage.delete(attachment);
            this.publisher.publish(createActionEvent(AttachmentActionEvent.AttachmentAction.deleted, attachment));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Nonnull
    @Override
    @PreAuthorize("hasGlobalPermission('USER')")
    public IAttachmentEntity getAttachment(@Nonnull final String uuid) {
        return this.attachmentStore.get(uuid);
    }

    /**
     * {@inheritDoc}
     */
    @Nonnull
    @Override
    // TODO used in AttachmentResource and DefaultSenderMessageHandler, in
    // DefaultSenderMessageHandler the call is
    // unsecured but not in a AttachmentResource
    @Unsecured("used internally in DefaultSenderMessageHandler")
    public IAttachmentEntity getAttachmentByFilename(@Nonnull final String filename) {
        return attachmentStore.getByFilename(filename);
    }

    public DownloadableAttachment getDownloadableAttachment(@Nonnull final String uuid) {
        final IFileStorageFile attachment = this.fileStorage.getByUuid(uuid);
        final IAttachmentEntity entity = this.attachmentStore.get(uuid);
        return new DownloadableAttachment(attachment, entity);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Unsecured("used internally in DefaultSenderMessageHandler")
    @Nonnull
    @Transactional
    public IAttachmentEntity updateSendStatus(@Nonnull final IAttachmentEntity attachment,
        @Nonnull final ISubmitterEntity submitter,
        @Nonnull final AttachmentSendStatus status) {
        return this.attachmentStore.updateSendStatus((AttachmentEntity) attachment, submitter, status);
    }

    @Nonnull
    @Override
    @PreAuthorize("hasGlobalPermission('USER')")
    public FsDirectory<?> moveDirectoryTo(@Nonnull final MoveDirectoryRequest request) throws IOException {
        IFileStorageDirectory directory = this.fileStorage.getDirectory(request.getPath());
        directory = this.fileStorage.moveTo(directory, request.getNewParentPath());
        return FsDirectory.createDirectory(directory);
    }

    @Nonnull
    @Override
    @PreAuthorize("hasGlobalPermission('USER')")
    public FsFile<AttachmentRequest> moveFileTo(@Nonnull final MoveFileRequest request) throws IOException {
        final IFileStorageFile file = this.fileStorage.getByUuid(request.getUuid());
        final IFileStorageFile newFile = this.fileStorage.moveTo(file, request.getNewParentPath());
        this.publisher.publish(new AttachmentMovedEvent(this, newFile.getUUID(), newFile.getName(),
                newFile.getMimeType().orElse(null), file.getRelativeParentPath().toString(),
                newFile.getRelativeParentPath().toString(), newFile.getSize()));
        return FsFile.createFile(file,
            AttachmentRequest.from(attachmentStore.get(((IFileStorageFile) newFile).getUUID())));
    }

    @Override
    @PreAuthorize("hasGlobalPermission('USER')")
    @Nonnull
    public FsTreeDirectory getTreeDirectory() {
        return FsTreeDirectory.create(this.fileStorage.walkTreeDirectories());
    }

    @Override
    @PreAuthorize("hasGlobalPermission('USER')")
    @Nonnull
    public FsDirectory<?> createDirectory(@Nonnull final String path) {
        return FsDirectory.createDirectory(fileStorage.createDirectory(path));
    }

    @Override
    @PreAuthorize("hasGlobalPermission('USER')")
    @Nonnull
    public FsDirectory<?> createDirectory(@Nonnull final FsDirectory<?> parent, @Nonnull final String name) {
        return FsDirectory.createDirectory(fileStorage.createDirectory(parent.getPath(), name));
    }

    @Override
    @PreAuthorize("hasGlobalPermission('USER')")
    public boolean deleteDirectory(@Nonnull final String name) {
        final IFileStorageDirectory directory = this.fileStorage.getDirectory(name);
        if (!directory.isEmpty()) {
            throw new EucegException(this.i18nService
                    .createKeyedMessage("app.service.euceg.attachment.directory.delete.not-empty", name));
        }
        return this.fileStorage.deleteDirectory(name);
    }

    @Override
    @PreAuthorize("hasGlobalPermission('USER')")
    public FsDirectory<?> renameDirectory(@Nonnull final String directory,
        @Nonnull final DirectoryUpateRequest request) {
        final IFileStorageDirectory storageDirectory = this.fileStorage.getDirectory(directory);
        return FsDirectory.createDirectory(this.fileStorage.renameDirectory(storageDirectory, request.getName()));
    }

    @Override
    @PreAuthorize("hasGlobalPermission('USER')")
    @Nonnull
    public Page<AttachmentRevision> findRevisions(@Nonnull final String uuid, @Nonnull final Pageable pageRequest) {
        return this.attachmentStore.findRevisions(uuid, pageRequest);
    }

    @Override
    @PreAuthorize("hasGlobalPermission('USER')")
    @Nonnull
    public AttachmentRevision getCurrentRevision(@Nonnull final String uuid) {
        return this.attachmentStore.getCurrentRevision(uuid);
    }

    @Override
    @PreAuthorize("hasGlobalPermission('USER')")
    public void checkIntegrity(final @Nonnull String filename) throws NonUniqueResultException {
        this.attachmentStore.findByFilename(filename).orElseThrow();

    }

    @Override
    @PreAuthorize("hasGlobalPermission('ADMIN')")
    public void fixIntegrity(final @Nonnull String filename) throws NonUniqueResultException {
        // try remove duplicate datarow with same filename
        List<AttachmentEntity> attrs = Lists.newArrayList(this.attachmentStore.findAllByFilename(filename));
        if (attrs.size() > 1) {
            // search better attachment to remove, i.e. not sent
            attrs.stream()
                    .filter(att -> !att.getDefaultStatus().isPresent()
                            || (att.getDefaultStatus().isPresent() && !att.getDefaultStatus().get().isSent()))
                    .findFirst()
                    .ifPresent(att -> this.attachmentStore.delete(att.getAttachmentId()));
        }
    }

    @Nonnull
    private AttachmentActionEvent createActionEvent(final @Nonnull AttachmentActionEvent.AttachmentAction action,
        final @Nonnull IFileStorageFile attachment) {
        return new AttachmentActionEvent(this, action, attachment.getUUID(), attachment.getName(),
                attachment.getMimeType().orElse(null), attachment.getRelativeParentPath().toString(),
                attachment.getSize());
    }
}
