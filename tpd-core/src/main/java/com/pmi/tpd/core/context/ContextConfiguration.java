package com.pmi.tpd.core.context;

import static com.pmi.tpd.api.util.Assert.checkNotNull;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.persistence.EntityManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;

import com.pmi.tpd.api.ApplicationConstants;
import com.pmi.tpd.api.config.IApplicationConfiguration;
import com.pmi.tpd.api.context.IApplicationProperties;
import com.pmi.tpd.api.context.IPropertiesManager;
import com.pmi.tpd.api.context.IPropertySetFactory;
import com.pmi.tpd.api.event.publisher.IEventPublisher;
import com.pmi.tpd.api.lifecycle.config.ApplicationStartedEvent;
import com.pmi.tpd.api.lifecycle.config.ApplicationStoppedEvent;
import com.pmi.tpd.api.lifecycle.config.ApplicationStoppingEvent;
import com.pmi.tpd.api.util.ClassLoaderUtils;
import com.pmi.tpd.api.versioning.IBuildUtilsInfo;
import com.pmi.tpd.core.context.propertyset.IPropertySetDAO;
import com.pmi.tpd.core.context.propertyset.spi.JpaPropertySetFactory;
import com.pmi.tpd.core.context.propertyset.spi.provider.JpaPropertySetDAOImpl;
import com.pmi.tpd.core.versioning.impl.BuildUtilsInfoGitImpl;
import com.pmi.tpd.spring.context.AbstractSmartLifecycle;
import com.pmi.tpd.spring.context.RelaxedPropertyResolver;
import com.pmi.tpd.spring.context.SystemOverridePropertiesFactoryBean;

/**
 * <p>
 * CoreConfig class.
 * </p>
 *
 * @author Christophe Friederich
 */
@Configuration
public class ContextConfiguration extends AbstractSmartLifecycle {

    /** */
    private static final Logger LOGGER = LoggerFactory.getLogger(ContextConfiguration.class);

    /** */
    @Inject
    private Environment environment;

    /** */
    @Inject
    private IEventPublisher eventPublisher;

    @PostConstruct
    public void init() {
        LOGGER.info("Configuration is initializing...");
        checkNotNull(environment, "environment");
        checkNotNull(eventPublisher, "eventPublisher");
    }

    @Override
    public int getPhase() {
        return ApplicationConstants.LifeCycle.LIFECYCLE_PHASE_CONFIG;
    }

    @Override
    public void start() {
        eventPublisher.publish(new ApplicationStartedEvent(this));

        super.start();
    }

    @Override
    public void stop() {
        eventPublisher.publish(new ApplicationStoppingEvent(this));
        eventPublisher.publish(new ApplicationStoppedEvent(this));

        super.stop();
    }

    @Bean(name = "sharedApplicationProperties")
    public SystemOverridePropertiesFactoryBean applicationProperties(final ResourceLoader resourceLoader) {
        final SystemOverridePropertiesFactoryBean propertiesFactoryBean = new SystemOverridePropertiesFactoryBean();
        propertiesFactoryBean.setIgnoreResourceNotFound(true);
        final String resolvedPath = environment.resolvePlaceholders(String.format("file:${%s}/%s",
            ApplicationConstants.PropertyKeys.SHARED_DIR_PATH_SYSTEM_PROPERTY,
            ApplicationConstants.CONFIG_PROPERTIES_FILE_NAME));
        propertiesFactoryBean.setLocations(resourceLoader.getResource(resolvedPath));
        return propertiesFactoryBean;
    }

    /**
     * @param propertySetFactory
     * @return
     */
    @Bean(IPropertiesManager.PROPERTIES_MANAGER_BEAN_NAME)
    @DependsOn(ApplicationConstants.Liquibase.LIQUIBASE_BEAN_NAME)
    public static IPropertiesManager propertiesManager(final IPropertySetFactory propertySetFactory,
        final Environment environment) {
        return new DefaultPropertiesManager(propertySetFactory, environment);
    }

    /**
     * @param eventPublisher
     * @param environment
     * @return
     */

    @Bean(IApplicationProperties.APPLICATION_PROPERTIES)
    public static IApplicationProperties applicationProperties(final IEventPublisher eventPublisher,
        final Provider<IPropertiesManager> propertiesManager,
        final Environment environment,
        final BeanFactory beanFactory) {
        return new DefaultApplicationProperties(eventPublisher, propertiesManager, environment, beanFactory);
    }

    /**
     * <p>
     * propertySetDAO.
     * </p>
     *
     * @return a {@link com.pmi.tpd.core.context.propertyset.IPropertySetDAO} object.
     */
    @Bean
    public IPropertySetDAO propertySetDAO(final EntityManager entityManager) {
        return new JpaPropertySetDAOImpl(entityManager);
    }

    /**
     * <p>
     * propertySetFactory.
     * </p>
     *
     * @param propertySetDAO
     *            a {@link com.pmi.tpd.core.context.propertyset.IPropertySetDAO} object.
     * @return a {@link com.pmi.tpd.api.context.IPropertySetFactory} object.
     */
    @Bean
    public IPropertySetFactory propertySetFactory(final IPropertySetDAO propertySetDAO) {
        return new JpaPropertySetFactory(propertySetDAO);
    }

    @Bean
    public IBuildUtilsInfo buildUtilsInfo() throws FileNotFoundException, RuntimeException {

        final RelaxedPropertyResolver properties = new RelaxedPropertyResolver(environment, "app.upgrade.");

        final long minimumUpgradableBuildNumber = Long
                .parseLong(properties.getProperty("minimum-upgradable.build-number", "0"));
        final String minimumUpgradableVersion = properties.getProperty("minimum-upgradable.version", "0.0.1");
        return new BuildUtilsInfoGitImpl(minimumUpgradableBuildNumber, minimumUpgradableVersion,
                loadProperties(IBuildUtilsInfo.BUILD_VERSIONS_PROPERTIES));
    }

    /**
     * @param info
     * @param environment
     * @return
     */
    @Bean
    public static IApplicationConfiguration applicationConfiguration(final IBuildUtilsInfo info,
        final Environment environment) {
        return new GlobalApplicationConfiguration(info, environment);
    }

    private static Properties loadProperties(final String file) throws RuntimeException, FileNotFoundException {
        final InputStream propsFile = ClassLoaderUtils.getResourceAsStream(file, ContextConfiguration.class);
        if (propsFile == null) {
            throw new IllegalStateException("File not found: " + file);
        }

        final Properties result = new Properties();
        try {
            result.load(propsFile);
        } catch (final IOException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                propsFile.close();
            } catch (final IOException e) {
                LOGGER.warn("closing property has failed", e);
            }
        }

        return result;
    }
}
