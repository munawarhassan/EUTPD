package com.pmi.tpd.core.euceg;

import java.util.concurrent.ScheduledExecutorService;
import java.util.logging.Level;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.persistence.EntityManager;

import org.eu.ceg.AppResponse;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.transaction.PlatformTransactionManager;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pmi.tpd.api.config.IApplicationConfiguration;
import com.pmi.tpd.api.context.IApplicationProperties;
import com.pmi.tpd.api.crypto.IKeyManagerProvider;
import com.pmi.tpd.api.crypto.IKeyProvider;
import com.pmi.tpd.api.event.publisher.IEventPublisher;
import com.pmi.tpd.api.i18n.I18nService;
import com.pmi.tpd.api.scheduler.IScheduledJobSource;
import com.pmi.tpd.api.scheduler.ISchedulerService;
import com.pmi.tpd.cluster.IClusterService;
import com.pmi.tpd.core.elasticsearch.IIndexerOperations;
import com.pmi.tpd.core.elasticsearch.repositories.IProductIndexedRepository;
import com.pmi.tpd.core.elasticsearch.repositories.ISubmissionIndexedRepository;
import com.pmi.tpd.core.euceg.impl.AttachmentStore;
import com.pmi.tpd.core.euceg.impl.BulkSendScheduler;
import com.pmi.tpd.core.euceg.impl.JpaAttachmentRepository;
import com.pmi.tpd.core.euceg.impl.JpaProductIdGeneratorRepository;
import com.pmi.tpd.core.euceg.impl.JpaProductRepository;
import com.pmi.tpd.core.euceg.impl.JpaProductSubmissionRepository;
import com.pmi.tpd.core.euceg.impl.JpaSubmitterRepository;
import com.pmi.tpd.core.euceg.impl.ProductStore;
import com.pmi.tpd.core.euceg.impl.ProductSubmissionStore;
import com.pmi.tpd.core.euceg.impl.SendAwaitPayloadScheduler;
import com.pmi.tpd.core.euceg.impl.SubmitterStore;
import com.pmi.tpd.core.euceg.report.DefaultEucegTaskExecutorManager;
import com.pmi.tpd.core.euceg.report.DefaultSubmissionReportTrackingService;
import com.pmi.tpd.core.euceg.report.IEucegTaskExecutorManager;
import com.pmi.tpd.core.euceg.report.ISubmissionReportTrackingService;
import com.pmi.tpd.core.euceg.spi.IAttachmentRepository;
import com.pmi.tpd.core.euceg.spi.IAttachmentStore;
import com.pmi.tpd.core.euceg.spi.IProductIdGeneratorRepository;
import com.pmi.tpd.core.euceg.spi.IProductRepository;
import com.pmi.tpd.core.euceg.spi.IProductStore;
import com.pmi.tpd.core.euceg.spi.IProductSubmissionRepository;
import com.pmi.tpd.core.euceg.spi.IProductSubmissionStore;
import com.pmi.tpd.core.euceg.spi.ISubmitterRepository;
import com.pmi.tpd.core.euceg.spi.ISubmitterStore;
import com.pmi.tpd.core.euceg.stat.DefaultEucegStatisticService;
import com.pmi.tpd.core.euceg.stat.IEucegStatisticService;
import com.pmi.tpd.core.security.ISecurityService;
import com.pmi.tpd.euceg.backend.core.BackendException;
import com.pmi.tpd.euceg.backend.core.BackendProperties;
import com.pmi.tpd.euceg.backend.core.DefaultEncryptionProvider;
import com.pmi.tpd.euceg.backend.core.IBackendManager;
import com.pmi.tpd.euceg.backend.core.IEncryptionProvider;
import com.pmi.tpd.euceg.backend.core.delivery.DefaultDeliveryBackendManager;
import com.pmi.tpd.euceg.backend.core.delivery.mock.MockDeliverySender;
import com.pmi.tpd.euceg.backend.core.domibus.api.ClientRest;
import com.pmi.tpd.euceg.backend.core.domibus.api.IClientRest;
import com.pmi.tpd.euceg.backend.core.spi.IPendingMessageProvider;
import com.pmi.tpd.euceg.backend.core.spi.ISenderMessageHandler;
import com.pmi.tpd.euceg.core.filestorage.DefaultFileStorage;
import com.pmi.tpd.euceg.core.filestorage.IFileStorage;
import com.pmi.tpd.euceg.core.task.DefaultEucegTaskFactory;
import com.pmi.tpd.euceg.core.task.IEucegTaskFactory;
import com.pmi.tpd.keystore.IKeyStoreService;
import com.pmi.tpd.security.IAuthenticationContext;
import com.pmi.tpd.security.random.ISecureTokenGenerator;
import com.pmi.tpd.spring.env.EnableConfigurationProperties;
import com.pmi.tpd.web.core.request.IRequestManager;

@Configuration
@EnableConfigurationProperties({ BackendProperties.class })
public class EucegConfiguration {

    @Inject
    @Nonnull
    private IApplicationProperties applicationProperties;

    /**
     * @param entityManagerFactory
     * @return
     */
    @Bean
    public IAttachmentRepository attachmentRepository(final EntityManager entityManager) {
        return new JpaAttachmentRepository(entityManager);
    }

    /**
     * @param entityManagerFactory
     * @return
     */
    @Bean
    public ISubmitterRepository submitterRepository(final EntityManager entityManager) {
        return new JpaSubmitterRepository(entityManager);
    }

    /**
     * @param entityManagerFactory
     * @return
     */
    @Bean
    public IProductSubmissionRepository productSubmissionRepository(final EntityManager entityManager) {
        return new JpaProductSubmissionRepository(entityManager);
    }

    /**
     * @param entityManagerFactory
     * @return
     */
    @Bean
    public IProductRepository productRepository(final EntityManager entityManager) {
        return new JpaProductRepository(entityManager);
    }

    /**
     * @param entityManagerFactory
     * @return
     */
    @Bean
    public IProductIdGeneratorRepository productIdGeneratorRepository(final EntityManager entityManager) {
        return new JpaProductIdGeneratorRepository(entityManager);
    }

    /**
     * @param repository
     * @return
     */
    @Bean
    public ISubmitterStore submitterStore(final ISubmitterRepository repository) {
        return new SubmitterStore(repository);
    }

    /**
     * @param repository
     * @param productIdGeneratorRepository
     * @return
     */
    @Bean
    public IProductSubmissionStore productSubmissionStore(final IProductSubmissionRepository repository,
        final IProductIdGeneratorRepository productIdGeneratorRepository) {
        return new ProductSubmissionStore(repository, productIdGeneratorRepository);
    }

    /**
     * @param attachmentRepository
     * @return
     */
    @Bean
    public IAttachmentStore attachmentStore(@Nonnull final IAttachmentRepository attachmentRepository) {
        return new AttachmentStore(attachmentRepository);
    }

    /**
     * @param repository
     * @return
     */
    @Bean
    public IProductStore productStore(final IProductRepository repository) {
        return new ProductStore(repository);
    }

    /**
     * @param settings
     * @param i18nService
     * @return
     */
    @Bean
    public IFileStorage fileStorage(@Nonnull final IApplicationConfiguration settings,
        @Nonnull final I18nService i18nService) {
        return new DefaultFileStorage(settings, i18nService);
    }

    /**
     * @param applicationProperties
     * @param securityKeyProvider
     * @return
     * @throws BackendException
     */
    @Bean
    public IEncryptionProvider encryptionProvider(@Nonnull final IKeyProvider securityKeyProvider)
            throws BackendException {
        return new DefaultEncryptionProvider(securityKeyProvider, applicationProperties);
    }

    @Bean
    public IClientRest clientRest(@Nonnull final IKeyManagerProvider keyManagerProvider,
        final @Nonnull I18nService i18nService) {
        final ClientRest client = new ClientRest(applicationProperties, i18nService, keyManagerProvider);
        client.setLogLevel(Level.WARNING);
        return client;
    }

    /**
     * @param keyStoreService
     * @param encryptionProvider
     * @param i18nService
     * @param applicationProperties
     * @return
     */
    @Bean
    public IBackendManager backendManager(final @Nonnull IClientRest clientRest,
        final @Nonnull TaskScheduler taskScheduler,
        final @Nonnull Provider<ISchedulerService> schedulerServiceProvider,
        final @Nonnull ISenderMessageHandler<AppResponse> senderMessageDelegate,
        final @Nonnull IEventPublisher eventPublisher,
        final @Nonnull IKeyStoreService keyStoreService,
        final @Nonnull IEncryptionProvider encryptionProvider,
        final @Nonnull IPendingMessageProvider pendingMessageProvider,
        final @Nonnull I18nService i18nService,
        final @Nonnull IApplicationConfiguration applicationConfiguration,
        final @Nonnull Environment environment) {
        final DefaultDeliveryBackendManager manager = new DefaultDeliveryBackendManager(clientRest,
                schedulerServiceProvider, eventPublisher, i18nService, encryptionProvider, applicationConfiguration,
                applicationProperties, pendingMessageProvider, keyStoreService);
        manager.setMessageHandler(senderMessageDelegate);
        if (environment.acceptsProfiles(Profiles.of("mock"))) {
            manager.setSender(new MockDeliverySender(eventPublisher, taskScheduler));
        }
        return manager;
    }

    @Bean
    public IAttachmentService attachmentService(@Nonnull final IFileStorage fileStorage,
        @Nonnull final IAttachmentStore attachmentStore,
        @Nonnull final I18nService i18nService,
        @Nonnull final IEventPublisher eventPublisher) {
        return new DefaultAttachmentService(attachmentStore, fileStorage, eventPublisher, i18nService);
    }

    @Bean
    public IEucegConstraintRuleManager constraintRuleManager(@Nonnull final IAttachmentStore attachmentStore,
        @Nonnull final I18nService i18nService) {
        return new DefaultConstraintRuleManager(attachmentStore, i18nService);
    }

    @Bean
    public IBulkProductService bulkProductService(final IProductStore productStore,
        final IProductIndexedRepository productIndexedRepository,
        final IAttachmentStore attachmentStore,
        final ISubmissionService submissionService,
        final IEucegConstraintRuleManager constraintRules,
        final I18nService i18nService,
        final ObjectMapper objectMappe) {
        return new DefaultBulkProductService(productStore, productIndexedRepository, attachmentStore, submissionService,
                constraintRules, i18nService, objectMappe);
    }

    /**
     * @param fileStorage
     * @param attachmentStore
     * @param submitterStore
     * @param productSubmissionStore
     * @param productStore
     * @param i18nService
     * @param backendService
     * @param eventPublisher
     * @param transactionManager
     * @return
     */
    @Bean
    public ISubmissionService submissionService(final @Nonnull IAttachmentService attachmentService,
        final @Nonnull IAttachmentStore attachmentStore,
        final @Nonnull ISubmitterStore submitterStore,
        final @Nonnull IProductSubmissionStore productSubmissionStore,
        final @Nonnull IProductStore productStore,
        final @Nonnull I18nService i18nService,
        final @Nonnull IBackendManager backendManager,
        final @Nonnull IEventPublisher eventPublisher,
        final @Nonnull PlatformTransactionManager transactionManager,
        final @Nonnull IEucegConstraintRuleManager constraintRuleManager,
        @Nonnull IAuthenticationContext authContext) {
        return new DefaultSubmissionService(applicationProperties, attachmentService, attachmentStore, submitterStore,
                productSubmissionStore, productStore, backendManager, i18nService, eventPublisher, transactionManager,
                authContext, constraintRuleManager);
    }

    /**
     * @param attachmentStore
     * @param submitterStore
     * @param productStore
     * @param i18nService
     * @param eventPublisher
     * @return
     */
    @Bean
    public IEucegImportExportService eucegImportExportService(@Nonnull final IAttachmentStore attachmentStore,
        @Nonnull final IFileStorage fileStorage,
        @Nonnull final ISubmitterStore submitterStore,
        @Nonnull final IProductStore productStore,
        @Nonnull final IProductSubmissionStore productSubmissionStore,
        @Nonnull final I18nService i18nService,
        @Nonnull final IEventPublisher eventPublisher) {
        return new DefaultEucegImportExportService(attachmentStore, fileStorage, submitterStore, productStore,
                productSubmissionStore, i18nService, eventPublisher);
    }

    @Bean
    public IEucegStatisticService eucegStatisticService(@Nonnull final ElasticsearchOperations operations,
        @Nonnull final ISubmissionIndexedRepository submissionIndexedRepository,
        @Nonnull final IProductIndexedRepository productIndexedRepository) {
        return new DefaultEucegStatisticService(operations, submissionIndexedRepository, productIndexedRepository);
    }

    @Bean
    public ISubmissionSenderManager submissionSenderManager(
        final @Nonnull PlatformTransactionManager platformTransactionManager,
        final @Nonnull Provider<IBackendManager> backendManager,
        final @Nonnull Provider<ISubmissionService> submissionServiceProvider,
        final @Nonnull IAttachmentService attachmentService,
        final @Nonnull ISubmitterStore submitterStore,
        final @Nonnull IProductSubmissionStore productSubmissionStore,
        final @Nonnull IProductStore productStore,
        final @Nonnull IEventPublisher eventPublisher) {
        return new DefaultSenderManager(platformTransactionManager, backendManager, submissionServiceProvider,
                attachmentService, submitterStore, productStore, productSubmissionStore, eventPublisher);
    }

    /**
     * @param awaitingPayloadJob
     * @return
     */
    @Bean
    public IScheduledJobSource sendAwaitPayloadScheduler(@Nonnull final ISendAwaitingPayloadJob awaitingPayloadJob) {
        return new SendAwaitPayloadScheduler(awaitingPayloadJob, applicationProperties);
    }

    /**
     * @param deferredSubmissionJob
     * @return
     */
    @Bean
    public IScheduledJobSource bulkSendScheduler(@Nonnull ISecurityService securityService,
        @Nonnull final ISendDeferredSubmissionJob deferredSubmissionJob) {
        return new BulkSendScheduler(deferredSubmissionJob, securityService, applicationProperties);
    }

    @Bean
    public IEucegTaskFactory eucegTaskFactory(final ApplicationContext applicationContext) {
        return new DefaultEucegTaskFactory(applicationContext);
    }

    @Bean
    public IEucegTaskExecutorManager eucegTaskExecutorManager(final @Nonnull ScheduledExecutorService executorService,
        final @Nonnull IEucegTaskFactory taskFactory,
        final @Nonnull I18nService i18nService,
        final ISecureTokenGenerator tokenGenerator,
        final IClusterService clusterService,
        final IRequestManager requestManager) {
        return new DefaultEucegTaskExecutorManager(executorService, taskFactory, i18nService, tokenGenerator,
                clusterService, requestManager);
    }

    @Bean
    public ISubmissionReportTrackingService submissionReportTrackingService(
        final @Nonnull IApplicationConfiguration settings,
        final @Nonnull IEucegTaskExecutorManager taskExecutorService,
        final @Nonnull IAttachmentService attachmentService,
        @Nonnull final IIndexerOperations indexerOperations,
        final IProductSubmissionStore productSubmissionStore,
        final IRequestManager requestManager,
        @Nonnull final I18nService i18nService) {
        return new DefaultSubmissionReportTrackingService(settings, taskExecutorService, attachmentService,
                indexerOperations, productSubmissionStore, requestManager, i18nService);
    }

}
