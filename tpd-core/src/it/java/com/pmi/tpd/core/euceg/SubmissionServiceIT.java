package com.pmi.tpd.core.euceg;

import java.nio.file.Path;

import javax.inject.Inject;
import javax.persistence.EntityManager;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.PlatformTransactionManager;

import com.pmi.tpd.api.context.IApplicationProperties;
import com.pmi.tpd.api.event.publisher.IEventPublisher;
import com.pmi.tpd.api.i18n.I18nService;
import com.pmi.tpd.api.i18n.support.SimpleI18nService;
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
import com.pmi.tpd.euceg.backend.core.IBackendManager;
import com.pmi.tpd.security.IAuthenticationContext;

@Configuration
@ContextConfiguration(classes = { SubmissionServiceIT.class })
public class SubmissionServiceIT extends BaseDaoTestIT {

    @Inject
    private IProductSubmissionStore productSubmissionStore;

    @Inject
    private IProductStore productStore;

    private IAttachmentService attachmentService;

    private IAttachmentStore attachmentStore;

    @Inject
    private ISubmitterStore submitterStore;

    @Inject
    private I18nService i18nService;

    @Inject
    private PlatformTransactionManager transactionManager;

    private IAuthenticationContext authContext;

    private IBackendManager backendManager;

    private IApplicationProperties applicationProperties;

    private IEventPublisher publisher;

    private IEucegConstraintRuleManager constraintRuleManager;

    public Path target;

    @SuppressWarnings("unused")
    private DefaultSubmissionService submissionService;

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

    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
        backendManager = mock(IBackendManager.class);
        attachmentService = mock(IAttachmentService.class);
        attachmentStore = mock(IAttachmentStore.class);
        publisher = mock(IEventPublisher.class);

        applicationProperties = mock(IApplicationProperties.class);
        authContext = mock(IAuthenticationContext.class);
        constraintRuleManager = mock(IEucegConstraintRuleManager.class);

        submissionService = new DefaultSubmissionService(applicationProperties, attachmentService, attachmentStore,
                submitterStore, productSubmissionStore, productStore, backendManager, i18nService, publisher,
                transactionManager, authContext, constraintRuleManager);
    }

}
