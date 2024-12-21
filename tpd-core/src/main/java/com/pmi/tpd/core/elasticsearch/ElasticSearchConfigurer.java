package com.pmi.tpd.core.elasticsearch;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import org.elasticsearch.client.Client;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.config.ElasticsearchConfigurationSupport;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.convert.ElasticsearchConverter;
import org.springframework.data.elasticsearch.core.convert.ElasticsearchCustomConversions;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;
import org.springframework.transaction.PlatformTransactionManager;

import com.pmi.tpd.api.config.IApplicationConfiguration;
import com.pmi.tpd.api.i18n.I18nService;
import com.pmi.tpd.core.elasticsearch.converter.DateTimeToLong;
import com.pmi.tpd.core.elasticsearch.converter.LocalDateToLong;
import com.pmi.tpd.core.elasticsearch.converter.LongToDateTime;
import com.pmi.tpd.core.elasticsearch.converter.LongToLocalDate;
import com.pmi.tpd.core.elasticsearch.impl.IndexerTaskFactory;
import com.pmi.tpd.core.elasticsearch.listener.AttachmentIndexingListener;
import com.pmi.tpd.core.elasticsearch.listener.ProductIndexingListener;
import com.pmi.tpd.core.elasticsearch.listener.SubmissionIndexingListener;
import com.pmi.tpd.core.elasticsearch.listener.SubmitterIndexingListener;
import com.pmi.tpd.core.elasticsearch.model.SubmissionIndexed;
import com.pmi.tpd.core.elasticsearch.repositories.IAttachmentIndexedRepository;
import com.pmi.tpd.core.elasticsearch.repositories.IProductIndexedRepository;
import com.pmi.tpd.core.elasticsearch.repositories.ISubmissionIndexedRepository;
import com.pmi.tpd.core.elasticsearch.repositories.ISubmitterIndexedRepository;
import com.pmi.tpd.core.elasticsearch.task.IIndexerTaskFactory;
import com.pmi.tpd.core.euceg.spi.IAttachmentStore;
import com.pmi.tpd.core.euceg.spi.IProductStore;
import com.pmi.tpd.core.euceg.spi.IProductSubmissionStore;
import com.pmi.tpd.core.euceg.spi.ISubmitterStore;
import com.pmi.tpd.core.maintenance.IMaintenanceService;
import com.pmi.tpd.spring.env.EnableConfigurationProperties;

/**
 * @author Christophe Friederich
 * @since 1.4
 */
@SuppressWarnings("deprecation")
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties({ ElasticSearchProperties.class })
@EnableElasticsearchRepositories(basePackageClasses = ISubmissionIndexedRepository.class, repositoryBaseClass = BaseElasticsearchRepository.class)
public class ElasticSearchConfigurer extends ElasticsearchConfigurationSupport {

  public static final String PREFIX_PROPERTY = "app.elasticsearch";

  @Inject
  private ElasticSearchProperties properties;

  @Inject
  private IApplicationConfiguration settings;

  @Bean
  @Conditional(UseEmbeddedServer.class)
  public NodeClientFactoryBean elasticsearchClient() {
    final boolean local = true;
    String clusterName = "tpd";
    if (settings.getEnvironment() != null) {
      clusterName += "-" + settings.getEnvironment().getName();
    }
    final NodeClientFactoryBean bean = new NodeClientFactoryBean(local);
    bean.setClusterName(clusterName);
    bean.setEnableHttp(true);
    bean.setPathData(settings.getIndexDirectory().toAbsolutePath().toString());
    bean.setPathHome(settings.getConfigurationDirectory().toAbsolutePath().toString());
    bean.setEnableMemoryLock(properties.isEnableMemoryLock());
    return bean;
  }

  @Bean
  @Conditional({ UseStandaloneServer.class })
  public RestClientFactoryBean restClient() {
    final ClientConfiguration clientConfiguration = ClientConfiguration.builder()
        .connectedTo(properties.getClusterNodes())
        .build();
    return new RestClientFactoryBean(clientConfiguration);
  }

  @Override
  protected Collection<String> getMappingBasePackages() {
    return Collections.singleton(SubmissionIndexed.class.getPackageName());
  }

  @Override
  @Bean
  public ElasticsearchCustomConversions elasticsearchCustomConversions() {
    return new ElasticsearchCustomConversions(Arrays
        .asList(new DateTimeToLong(), new LongToDateTime(), new LongToLocalDate(), new LocalDateToLong()));
  }

  @Bean(name = { "elasticsearchOperations", "elasticsearchTemplate" })
  @Conditional(UseEmbeddedServer.class)
  public ElasticsearchOperations elasticsearchOperations(final ElasticsearchConverter elasticsearchConverter,
      final Client client) {
    final ElasticsearchTemplate template = new ElasticsearchTemplate(client, elasticsearchConverter);
    return template;
  }

  @Bean(name = { "elasticsearchOperations", "elasticsearchTemplate" })
  @Conditional({ UseStandaloneServer.class })
  public ElasticsearchOperations restOperations(final ElasticsearchConverter elasticsearchConverter,
      final RestHighLevelClient elasticsearchClient) {
    final ElasticsearchRestTemplate template = new ElasticsearchRestTemplate(elasticsearchClient,
        elasticsearchConverter);
    template.setRefreshPolicy(refreshPolicy());
    return template;
  }

  @Bean()
  @DependsOn("elasticsearchOperations")
  public IIndexerOperations indexerOperations(final ElasticsearchOperations elasticsearchTemplate,
      final I18nService i18nService,
      @Nonnull final ISubmissionIndexedRepository submissionIndexRepository,
      @Nonnull final IProductIndexedRepository productIndexRepository,
      @Nonnull final ISubmitterIndexedRepository submitterIndexedRepository,
      @Nonnull final IAttachmentIndexedRepository attachmentIndexedRepository) {
    return new DefaultIndexerOperations(elasticsearchTemplate, i18nService, submissionIndexRepository,
        productIndexRepository, submitterIndexedRepository, attachmentIndexedRepository);
  }

  @Bean
  public IIndexerTaskFactory indexerTaskFactory(final ApplicationContext applicationContext) {
    return new IndexerTaskFactory(applicationContext);
  }

  @Bean
  public IIndexerService indexerService(final IIndexerTaskFactory taskFactory,
      final IMaintenanceService maintenanceService,
      final I18nService i18nService,
      final IIndexerOperations indexerOperations,
      final IProductSubmissionStore productSubmissionStore,
      final IProductStore productStore,
      final ISubmitterStore submitterStore,
      final IAttachmentStore attachmentStore,
      final PlatformTransactionManager transactionManager) {
    return new DefaultIndexerService(taskFactory, maintenanceService, i18nService, indexerOperations,
        productSubmissionStore, productStore, submitterStore, attachmentStore, transactionManager);
  }

  @Bean
  public SubmissionIndexingListener submissionListener(final IIndexerOperations indexerOperations) {
    final SubmissionIndexingListener listener = new SubmissionIndexingListener(indexerOperations);
    return listener;
  }

  @Bean
  public ProductIndexingListener productListener(final IIndexerOperations indexerOperations) {
    final ProductIndexingListener listener = new ProductIndexingListener(indexerOperations);
    return listener;
  }

  @Bean
  public SubmitterIndexingListener submitterListener(final IIndexerOperations indexerOperations) {
    final SubmitterIndexingListener listener = new SubmitterIndexingListener(indexerOperations);
    return listener;
  }

  @Bean
  public AttachmentIndexingListener attachmentListener(final IIndexerOperations indexerOperations) {
    final AttachmentIndexingListener listener = new AttachmentIndexingListener(indexerOperations);
    return listener;
  }

  private static class UseEmbeddedServer implements Condition {

    @Override
    public boolean matches(final ConditionContext context, final AnnotatedTypeMetadata metadata) {
      final Boolean useEmbedded = context.getEnvironment()
          .getProperty(PREFIX_PROPERTY + ".useEmbedded", Boolean.class);
      return useEmbedded == null ? false : useEmbedded;
    }
  }

  private static class UseStandaloneServer implements Condition {

    @Override
    public boolean matches(final ConditionContext context, final AnnotatedTypeMetadata metadata) {
      final Boolean useEmbedded = context.getEnvironment()
          .getProperty(PREFIX_PROPERTY + ".useEmbedded", Boolean.class);
      return useEmbedded == null ? false : !useEmbedded;
    }
  }

}
