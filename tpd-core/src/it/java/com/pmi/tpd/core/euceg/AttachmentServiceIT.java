package com.pmi.tpd.core.euceg;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;

import javax.inject.Inject;
import javax.persistence.EntityManager;

import org.apache.commons.io.IOUtils;
import org.eu.ceg.AttachmentAction;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import com.google.common.io.ByteSource;
import com.pmi.tpd.api.config.IApplicationConfiguration;
import com.pmi.tpd.api.event.publisher.IEventPublisher;
import com.pmi.tpd.api.i18n.I18nService;
import com.pmi.tpd.api.i18n.support.SimpleI18nService;
import com.pmi.tpd.api.paging.PageUtils;
import com.pmi.tpd.core.BaseDaoTestIT;
import com.pmi.tpd.core.euceg.impl.AttachmentStore;
import com.pmi.tpd.core.euceg.impl.JpaAttachmentRepository;
import com.pmi.tpd.core.euceg.impl.JpaProductIdGeneratorRepository;
import com.pmi.tpd.core.euceg.impl.JpaProductRepository;
import com.pmi.tpd.core.euceg.impl.JpaProductSubmissionRepository;
import com.pmi.tpd.core.euceg.impl.JpaSubmitterRepository;
import com.pmi.tpd.core.euceg.impl.ProductStore;
import com.pmi.tpd.core.euceg.impl.ProductSubmissionStore;
import com.pmi.tpd.core.euceg.impl.SubmitterStore;
import com.pmi.tpd.core.euceg.spi.IAttachmentRepository;
import com.pmi.tpd.core.euceg.spi.IAttachmentStore;
import com.pmi.tpd.core.euceg.spi.IProductIdGeneratorRepository;
import com.pmi.tpd.core.euceg.spi.IProductRepository;
import com.pmi.tpd.core.euceg.spi.IProductStore;
import com.pmi.tpd.core.euceg.spi.IProductSubmissionRepository;
import com.pmi.tpd.core.euceg.spi.IProductSubmissionStore;
import com.pmi.tpd.core.euceg.spi.ISubmitterRepository;
import com.pmi.tpd.core.euceg.spi.ISubmitterStore;
import com.pmi.tpd.core.model.euceg.AttachmentEntity;
import com.pmi.tpd.core.model.euceg.StatusAttachment;
import com.pmi.tpd.core.model.euceg.StatusAttachment.StatusAttachmentId;
import com.pmi.tpd.euceg.api.Eucegs;
import com.pmi.tpd.euceg.core.filestorage.DefaultFileStorage;
import com.pmi.tpd.euceg.core.filestorage.IFileStorage;

@Configuration
@ContextConfiguration(classes = { AttachmentServiceIT.class })
public class AttachmentServiceIT extends BaseDaoTestIT {

    private IFileStorage fileStorage;

    @Inject
    private IAttachmentStore attachmentStore;

    @Inject
    private IAttachmentRepository attachmentRepository;

    @Inject
    private I18nService i18nService;

    private IApplicationConfiguration applicationConfiguration;

    private IEventPublisher publisher;

    public Path target;

    private DefaultAttachmentService attachmentService;

    @Bean
    public I18nService i18nService() {
        return new SimpleI18nService();
    }

    @Bean
    public IProductSubmissionRepository productSubmissionRepository(final EntityManager entityManager) {
        return new JpaProductSubmissionRepository(entityManager);
    }

    @Bean
    public IProductRepository productRepository(final EntityManager entityManager) {
        return new JpaProductRepository(entityManager);
    }

    @Bean
    public IAttachmentRepository attachmentRepository(final EntityManager entityManager) {
        return new JpaAttachmentRepository(entityManager);
    }

    @Bean
    public ISubmitterRepository submitterRepository(final EntityManager entityManager) {
        return new JpaSubmitterRepository(entityManager);
    }

    @Bean
    public IProductIdGeneratorRepository productIdGeneratorRepository(final EntityManager entityManager) {
        return new JpaProductIdGeneratorRepository(entityManager);
    }

    @Bean
    public ISubmitterStore submitterStore(final ISubmitterRepository repository) {
        return new SubmitterStore(repository);
    }

    @Bean
    public IAttachmentStore attachmentStore(final IAttachmentRepository attachmentRepository) {
        return new AttachmentStore(attachmentRepository);
    }

    @Bean
    public IProductSubmissionStore productSubmissionStore(final IProductSubmissionRepository repository,
        final IProductIdGeneratorRepository productIdGeneratorRepository) {
        return new ProductSubmissionStore(repository, productIdGeneratorRepository);
    }

    @Bean
    public IProductStore productStore(final IProductRepository repository) {
        return new ProductStore(repository);
    }

    @BeforeEach
    public void setUp(@TempDir final Path path) throws Exception {
        super.setUp();
        this.target = path;

        applicationConfiguration = mock(IApplicationConfiguration.class, withSettings().lenient());
        publisher = mock(IEventPublisher.class);

        final Path attFolder = target.resolve("attachments");
        attFolder.toFile().mkdir();
        when(applicationConfiguration.getAttachmentsDirectory()).thenReturn(attFolder);
        fileStorage = new DefaultFileStorage(applicationConfiguration, i18nService);
        attachmentService = new DefaultAttachmentService(attachmentStore, fileStorage, publisher, i18nService);
    }

    /**
     * @throws IOException
     * @throws ConcurrencyAttachmentAccessException
     * @throws AttachmentInvalidFilenaneException
     */
    @Test
    public void shouldStoreNewAttachment()
            throws IOException, ConcurrencyAttachmentAccessException, AttachmentInvalidFilenaneException {
        final String filename = "attachment.pdf";
        final String content = "content";

        try (InputStream in = ByteSource.wrap(content.getBytes()).openStream()) {
            attachmentService.storeAttachment(in, filename, "application/pdf", true);
        }
        final AttachmentEntity attachment = attachmentStore.getByFilename(filename);
        assertThat(attachment, Matchers.notNullValue());
        assertThat(attachment.getFilename(), Matchers.is(filename));
        try (InputStream stream = fileStorage.getByName(attachment.getFilename()).openStream()) {
            assertThat(IOUtils.toString(stream, Charsets.UTF_8), Matchers.is(content));
        }
    }

    @Test
    @Transactional
    public void shouldNotStoreNewAttachmentOnRuntimeException()
            throws IOException, ConcurrencyAttachmentAccessException, AttachmentInvalidFilenaneException {
        final String filename = "attachment.pdf";
        final String content = "content";
        final IAttachmentStore anAttachmentStore = mock(IAttachmentStore.class);
        attachmentService = new DefaultAttachmentService(anAttachmentStore, fileStorage, publisher, i18nService);

        when(anAttachmentStore.create(any(AttachmentEntity.class))).thenThrow(RuntimeException.class);

        try (InputStream in = ByteSource.wrap(content.getBytes()).openStream()) {
            attachmentService.storeAttachment(in, filename, "application/pdf", true);
            fail();
        } catch (final RuntimeException ex) {

        }
        assertEquals(0, this.attachmentStore.count());
        assertEquals(false, fileStorage.exists(filename));

    }

    @Test
    public void shouldUpdateExistingAttachment()
            throws IOException, ConcurrencyAttachmentAccessException, AttachmentInvalidFilenaneException {
        final String filename = "attachment.pdf";
        final String content = "content";

        try (InputStream in = ByteSource.wrap(content.getBytes()).openStream()) {
            attachmentService.storeAttachment(in, filename, "application/pdf", true);
        }
        AttachmentEntity attachment = attachmentStore.getByFilename(filename);
        try (InputStream stream = fileStorage.getByName(attachment.getFilename()).openStream()) {
            assertThat(IOUtils.toString(stream, Charsets.UTF_8), Matchers.is(content));
        }

        try (InputStream in = ByteSource.wrap("new content".getBytes()).openStream()) {
            attachmentService.storeAttachment(in, filename, "application/pdf", true);
        }
        attachment = attachmentStore.getByFilename(filename);
        try (InputStream stream = fileStorage.getByName(attachment.getFilename()).openStream()) {
            assertThat(IOUtils.toString(stream, Charsets.UTF_8), Matchers.is("new content"));
        }
    }

    @Test
    @Transactional
    public void shouldNotUpdateExistingAttachmentOnConcurrencyAttachmentAccessException()
            throws IOException, ConcurrencyAttachmentAccessException, AttachmentInvalidFilenaneException {
        final String filename = "attachment.pdf";
        final String content = "content";
        final IAttachmentStore anAttachmentStore = spy(new AttachmentStore(attachmentRepository));
        attachmentService = new DefaultAttachmentService(anAttachmentStore, fileStorage, publisher, i18nService);

        AttachmentEntity attachmentEntity = null;
        try (InputStream in = ByteSource.wrap(content.getBytes()).openStream()) {
            attachmentEntity = attachmentService.storeAttachment(in, filename, "application/pdf", true);
        }
        assertEquals(1, attachmentEntity.getVersion());

        try (InputStream in = ByteSource.wrap("new content".getBytes()).openStream()) {
            attachmentService.storeAttachment(in, filename, "application/pdf", true);
        } catch (final ConcurrencyAttachmentAccessException ex) {

        }
        assertEquals(1, attachmentEntity.getVersion());
        assertEquals(1, this.attachmentStore.count());
        assertEquals(true, fileStorage.exists(filename));
        // normally, the file should not be updated
        // TODO implement temporary store and confirmation after success.
        try (InputStream stream = fileStorage.getByName(filename).openStream()) {
            assertThat(IOUtils.toString(stream, Charsets.UTF_8), Matchers.is("new content"));
        }
    }

    /**
     * Can not update attachment with same name but with different case.
     *
     * @throws IOException
     * @throws ConcurrencyAttachmentAccessException
     * @throws AttachmentInvalidFilenaneException
     * @see TPD-145
     */
    @Test
    public void shouldFilenameAttachmentCaseSensitive()
            throws IOException, ConcurrencyAttachmentAccessException, AttachmentInvalidFilenaneException {
        final String filename = "attachment.pdf";
        final String caseFilename = "Attachment.pdf";

        try (InputStream in = ByteSource.wrap("content".getBytes()).openStream()) {
            attachmentService.storeAttachment(in, filename, "application/pdf", true);
        }
        try (InputStream in = ByteSource.wrap("content".getBytes()).openStream()) {
            attachmentService.storeAttachment(in, caseFilename, "application/pdf", true);
        }

        AttachmentEntity attachment = attachmentStore.getByFilename(filename);
        assertThat(attachment, Matchers.notNullValue());
        assertThat(attachment.getFilename(), Matchers.is(filename));

        attachment = attachmentStore.getByFilename(caseFilename);
        assertThat(attachment, Matchers.notNullValue());
        assertThat(attachment.getFilename(), Matchers.is(caseFilename));

        final Page<AttachmentEntity> page = this.attachmentStore.findAll(PageUtils.newRequest(0, 5));
        assertThat(page.getNumberOfElements(), Matchers.is(2));

    }

    /**
     * A attachment filename should accept ',' character.
     *
     * @throws IOException
     * @throws ConcurrencyAttachmentAccessException
     * @throws AttachmentInvalidFilenaneException
     * @see TPD-203
     */
    @Test
    public void shouldFilenameAttachmentAcceptComma()
            throws IOException, ConcurrencyAttachmentAccessException, AttachmentInvalidFilenaneException {
        final String filename = "2,(4)-heptadienal .THS_PL.pdf";
        try (InputStream in = ByteSource.wrap("content".getBytes()).openStream()) {
            attachmentService.storeAttachment(in, filename, "application/pdf", true);
        }
        // second time
        try (InputStream in = ByteSource.wrap("content".getBytes()).openStream()) {
            attachmentService.storeAttachment(in, filename, "application/pdf", true);
        }

        final AttachmentEntity attachment = attachmentStore.getByFilename(filename);
        assertThat(attachment, Matchers.notNullValue());
        assertThat(attachment.getFilename(), Matchers.is(filename));

        final Page<AttachmentEntity> page = this.attachmentStore.findAll(PageUtils.newRequest(0, 5));
        assertThat(page.getNumberOfElements(), Matchers.is(1));
    }

    @Test
    public void shouldNotFilenameAttachmentAcceptUnexpectedCharacters()
            throws IOException, ConcurrencyAttachmentAccessException, AttachmentInvalidFilenaneException {
        Assertions.assertThrows(AttachmentInvalidFilenaneException.class, () -> {
            final String filename = "2,4ƒ∂åptadienal_THS_PL.pdf";
            try (InputStream in = ByteSource.wrap("content".getBytes()).openStream()) {
                attachmentService.storeAttachment(in, filename, "application/pdf", true);
            }
        });
    }

    @Test
    public void shouldFixDuplicatAttachment()
            throws IOException, ConcurrencyAttachmentAccessException, AttachmentInvalidFilenaneException {
        final String filename = "2,(4)-heptadienal .THS_PL.pdf";
        AttachmentEntity att1 = attachmentStore.create(AttachmentEntity.builder()
                .attachmentId(Eucegs.uuid())
                .filename(filename)
                .confidential(false)
                .contentType("application/pdf")
                .build());
        attachmentStore.update(att1.copy()
                .status(StatusAttachment.builder()
                        .id(StatusAttachmentId.key(att1.getAttachmentId(), "966667"))
                        .sent()
                        .action(AttachmentAction.CREATE)
                        .build())
                .build());

        attachmentStore.create(AttachmentEntity.builder()
                .attachmentId(Eucegs.uuid())
                .filename(filename)
                .confidential(false)
                .contentType("application/pdf")
                .build());
        Assertions.assertThrows(IncorrectResultSizeDataAccessException.class,
            () -> this.attachmentService.checkIntegrity(filename));

        List<AttachmentEntity> attrs = Lists.newArrayList(this.attachmentStore.findAllByFilename(filename));
        assertThat(attrs.size(), Matchers.is(2));

        this.attachmentService.fixIntegrity(filename);
        attrs = Lists.newArrayList(this.attachmentStore.findAllByFilename(filename));
        assertThat(attrs.size(), Matchers.is(1));
    }

}
