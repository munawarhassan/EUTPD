package com.pmi.tpd.core;

import java.util.List;
import java.util.Optional;

import javax.annotation.Nonnull;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

import org.hibernate.cfg.AvailableSettings;
import org.hibernate.envers.configuration.EnversSettings;
import org.hibernate.jpa.boot.internal.EntityManagerFactoryBuilderImpl;
import org.hibernate.jpa.boot.spi.IntegratorProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Scope;
import org.springframework.dao.annotation.PersistenceExceptionTranslationPostProcessor;
import org.springframework.dao.support.PersistenceExceptionTranslator;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.support.SharedEntityManagerBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import com.fasterxml.jackson.datatype.hibernate5.Hibernate5Module;
import com.google.common.collect.ImmutableMap;
import com.pmi.tpd.api.ApplicationConstants;
import com.pmi.tpd.api.context.annotation.IntegrationTest;
import com.pmi.tpd.api.context.annotation.Test;
import com.pmi.tpd.core.cache.CacheConfiguration;
import com.pmi.tpd.database.IDataSourceConfiguration;
import com.pmi.tpd.database.hibernate.JpaEntityListenersIntegrator;
import com.pmi.tpd.database.jpa.ConfigurableLocalContainerEntityManagerFactoryBean;
import com.pmi.tpd.database.jpa.DelegatingSwappableEntityManagerFactory;
import com.pmi.tpd.database.jpa.JpaPersistenceExceptionTranslator;
import com.pmi.tpd.spring.context.RelaxedPropertyResolver;
import com.pmi.tpd.spring.transaction.DefaultTransactionSynchronizer;
import com.pmi.tpd.spring.transaction.ITransactionSynchronizer;

/**
 * <p>
 * JpaConfig class.
 * </p>
 *
 * @author Christophe Friederich
 * @since 1.0
 */
@Configuration
@EnableTransactionManagement
@EnableJpaAuditing(auditorAwareRef = "springSecurityAuditorAware")
@Import({ DatabaseConfig.class, MemoryDatabaseConfig.class, CacheConfiguration.class })
@DependsOn({ CacheConfiguration.HAZELCAST_CACHE_MANAGER_BEAN_NAME })
public class JpaConfig implements EnvironmentAware {

    private static Logger LOGGER = LoggerFactory.getLogger(JpaConfig.class);

    private final HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();

    /** */
    private RelaxedPropertyResolver props;

    private LocalContainerEntityManagerFactoryBean localEntityManagerFactoryBean;

    @Inject
    private ApplicationContext context;

    /** {@inheritDoc} */
    @Override
    public void setEnvironment(final org.springframework.core.env.Environment environment) {
        this.props = new RelaxedPropertyResolver(environment, "jpa.");
    }

    @PostConstruct
    public void configure() {
        vendorAdapter.setShowSql(props.getProperty("show_sql", Boolean.class, false));
        // managed by liquibase
        vendorAdapter.setGenerateDdl(props.getProperty("generate-ddl", Boolean.class, false));
    }

    @Bean
    public JpaEntityListenersIntegrator listenersIntegrator() {
        return new JpaEntityListenersIntegrator(context);
    }

    /**
     * <p>
     * entityManagerFactory.
     * </p>
     *
     * @param dataSource
     *                   a {@link javax.sql.DataSource} object.
     * @return a {@link org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean} object.
     * @throws java.lang.ClassNotFoundException
     *                                          if any.
     */
    @Bean(name = "prototypeEntityManagerFactory", destroyMethod = "")
    @Scope(scopeName = BeanDefinition.SCOPE_PROTOTYPE)
    public LocalContainerEntityManagerFactoryBean prototypeEntityManagerFactory(
        @Nonnull @Named("dataSource") final DataSource dataSource,

        final IDataSourceConfiguration dataSourceConfiguration,
        final @Value("false") boolean enforce) throws ClassNotFoundException {

        if (!enforce && localEntityManagerFactoryBean != null) {
            return localEntityManagerFactoryBean;
        }
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("Creating new Entity Manager Factory prototype: {}", dataSourceConfiguration);
        }
        final ConfigurableLocalContainerEntityManagerFactoryBean lef = new ConfigurableLocalContainerEntityManagerFactoryBean(
                dataSource, dataSourceConfiguration);
        lef.setJpaVendorAdapter(vendorAdapter);

        lef.setPackagesToScan(ApplicationConstants.Jpa.MODEL_PACKAGE);
        lef.setJpaPropertyMap(ImmutableMap.<String, Object> builder()
                // .put(AvailableSettings.NAMING_STRATEGY,
                // props.getProperty("hibernate.naming-strategy", "org.hibernate.cfg.EJB3NamingStrategy"))
                // see
                // http://docs.jboss.org/hibernate/orm/5.2/userguide/html_single/Hibernate_User_Guide.html#identifiers-generators
                .put(AvailableSettings.GENERATE_STATISTICS,
                    props.getProperty("hibernate.generate_statistics", Boolean.class, false))
                .put(AvailableSettings.USE_QUERY_CACHE,
                    props.getProperty("hibernate.cache.use_query_cache", Boolean.class, false))
                .put(AvailableSettings.USE_SECOND_LEVEL_CACHE,
                    props.getProperty("hibernate.cache.use_second_level_cache", Boolean.class, false))
                // TODO modify when use hazelcast
                .put(AvailableSettings.CACHE_REGION_FACTORY,
                    props.getProperty("hibernate.cache.region.factory_class", String.class, "jcache"))
                .put("hibernate.javax.cache.provider",
                    props.getProperty("hibernate.cache.provider",
                        String.class,
                        "com.hazelcast.cache.HazelcastCachingProvider"))
                .put("hibernate.javax.cache.uri", props.getProperty("hibernate.cache.uri", String.class, null))
                .put(org.hibernate.jpa.AvailableSettings.ENTITY_MANAGER_FACTORY_NAME,
                    ApplicationConstants.Jpa.ENTITY_MANAGER_FACTORY_NAME)
                .put("hibernate.temp.use_jdbc_metadata_defaults", true)
                .put("hibernate.event.merge.entity_copy_observer", "allow")
                // envers configuration
                // .put(EnversSettings.REVISION_FIELD_NAME, "rev")
                // .put(EnversSettings.REVISION_TYPE_FIELD_NAME, "revtype")
                .put(EnversSettings.AUDIT_TABLE_SUFFIX, "_aud")
                .put(EnversSettings.DO_NOT_AUDIT_OPTIMISTIC_LOCKING_FIELD, false)
                .put(EntityManagerFactoryBuilderImpl.INTEGRATOR_PROVIDER,
                    (IntegratorProvider) () -> List.of(listenersIntegrator()))
                .build());
        localEntityManagerFactoryBean = lef;
        return lef;
    }

    /**
     * @param delegate
     * @return
     */
    @Bean(name = ApplicationConstants.Jpa.ENTITY_MANAGER_FACTORY_NAME)
    public static EntityManagerFactory entityManagerFactory(
        @Named("prototypeEntityManagerFactory") final EntityManagerFactory delegate,
        final ApplicationContext context) {
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("Creating new Delegate Entity Manager Factory from delegate: {}", delegate);
        }
        return new DelegatingSwappableEntityManagerFactory(delegate, context);
    }

    @Bean
    public SharedEntityManagerBean entityManager(
        @Named(ApplicationConstants.Jpa.ENTITY_MANAGER_FACTORY_NAME) final EntityManagerFactory emf,
        final BeanFactory beanFactory) {
        final SharedEntityManagerBean managerBean = new SharedEntityManagerBean();
        managerBean.setEntityManagerFactory(emf);
        managerBean.setSynchronizedWithTransaction(true);
        managerBean.setBeanFactory(beanFactory);
        return managerBean;
    }

    @Bean
    public static ITransactionSynchronizer transactionSynchronizer() {
        return new DefaultTransactionSynchronizer();
    }

    /**
     * <p>
     * hibernate5Module.
     * </p>
     *
     * @return a {@link Hibernate5Module} object.
     */
    @Bean
    public Hibernate5Module hibernate5Module() {
        return new Hibernate5Module();
    }

    /**
     * <p>
     * transactionManager.
     * </p>
     *
     * @param entityManagerFactory
     *                             a {@link javax.persistence.EntityManagerFactory} object.
     * @return a {@link org.springframework.transaction.PlatformTransactionManager} object.
     */
    @Bean()
    public PlatformTransactionManager transactionManager(
        @Named(ApplicationConstants.Jpa.ENTITY_MANAGER_FACTORY_NAME) final EntityManagerFactory entityManagerFactory) {
        final JpaTransactionManager txManager = new JpaTransactionManager();
        txManager.setGlobalRollbackOnParticipationFailure(false);
        txManager.setEntityManagerFactory(entityManagerFactory);
        return txManager;
    }

    /**
     * @return
     */
    @Bean
    public BeanPostProcessor persistenceExceptionTranslation() {
        final PersistenceExceptionTranslationPostProcessor postProcessor = //
                new PersistenceExceptionTranslationPostProcessor();
        // allows as well translate validation exception.
        // postProcessor.setBeforeExistingAdvisors(true);
        return postProcessor;
    }

    /**
     * @param vendorAdapter
     * @return
     */
    @Bean
    public PersistenceExceptionTranslator jpaPersistenceExceptionTranslator() {
        return new JpaPersistenceExceptionTranslator(vendorAdapter);
    }

    /**
     * <p>
     * springSecurityAuditorAware.
     * </p>
     *
     * @return a {@link org.springframework.data.domain.AuditorAware} object.
     */
    @Test
    @IntegrationTest
    @Bean(name = "springSecurityAuditorAware")
    public static AuditorAware<String> springSecurityAuditorAware() {
        return () -> Optional.of("userAudit");
    }
}
