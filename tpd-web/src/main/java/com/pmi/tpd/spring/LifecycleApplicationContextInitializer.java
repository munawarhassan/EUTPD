package com.pmi.tpd.spring;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.SmartInstantiationAwareBeanPostProcessor;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ApplicationListener;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;

import com.pmi.tpd.api.ApplicationConstants;
import com.pmi.tpd.api.exec.ProgressImpl;
import com.pmi.tpd.core.lifecycle.StartupProgressEvent;
import com.pmi.tpd.startup.IStartupManager;
import com.pmi.tpd.startup.StartupUtils;

/**
 * @author Christophe Friederich
 * @since 1.3
 */
public class LifecycleApplicationContextInitializer
        implements ApplicationContextInitializer<AnnotationConfigWebApplicationContext> {

    private static final Logger LOGGER = LoggerFactory.getLogger(LifecycleApplicationContextInitializer.class);

    /** */
    private volatile int counter = 0;

    private IStartupManager startupManager;

    @Override
    public void initialize(final AnnotationConfigWebApplicationContext applicationContext) {
        counter = 0;
        startupManager = StartupUtils.getStartupManager(applicationContext.getServletContext());

        applicationContext.addApplicationListener(new StartupProgressApplicationListener());
        applicationContext.addBeanFactoryPostProcessor(new StartupBeanFactoryPostProcessor());
    }

    public void publishProgress(final String message, final int size) {
        counter += size;
        if (LOGGER.isDebugEnabled()) {
            LOGGER.warn("the current progress value '{}'", counter);
        }
        if (counter >= 100) {
            LOGGER.warn("the progress value '{}' exceed 100", counter);
            // reduce size
            counter = 90;
        }
        startupManager.onProgress(new ProgressImpl(message, counter));
    }

    /**
     * @author Christophe Friederich
     */
    private final class StartupBeanFactoryPostProcessor
            implements SmartInstantiationAwareBeanPostProcessor, BeanFactoryPostProcessor {

        @Override
        public void postProcessBeanFactory(final ConfigurableListableBeanFactory beanFactory) {
            publishProgress("Initializing Spring context", 4);

            beanFactory.registerSingleton("startupManager", startupManager);
            beanFactory.addBeanPostProcessor(this);
        }

        @Override
        public Object postProcessBeforeInitialization(final Object bean, final String beanName) {
            switch (beanName) {
                case "homeLockAcquirer":
                    updateProgress("Acquiring home directory lock", 2);
                    break;
                case "homeDirectoryMigration":
                    updateProgress("Migrating home directory", 3);
                    break;
                case "liquibase":
                    updateProgress("Connecting to database and updating schema", 20);
                    break;
                case ApplicationConstants.Jpa.ENTITY_MANAGER_FACTORY_NAME:
                    if (startupManager.isStarting()) {
                        updateProgress("Initializing Hibernate and validating schema", 10);
                    }
                    break;
                case "entityManager":
                    if (startupManager.isStarting()) {
                        updateProgress("Initializing JPA", 5);
                    }
                    break;
                case "localScheduler":
                    updateProgress("Initializing Local Scheduler", 5);
                    break;
                case "clusteredScheduler":
                    updateProgress("Initializing Clustered Scheduler", 5);
                    break;
                case "schedulerService":
                    updateProgress("Initializing Scheduler", 15);
                    break;
                default:
                    break;

            }

            return bean;
        }

        @Override
        public Object postProcessBeforeInstantiation(final Class<?> beanClass, final String beanName)
                throws BeansException {
            if ("HazelcastConfigFactoryBean".equals(beanClass.getSimpleName())) {
                // Hazelcast uses a factory method, so this will not be invoked for the "hazelcast" bean.
                // Hazelcast connects to the cluster inside the factory method's processing, so if we wait
                // for even postProcessBeforeInitialization on the "hazelcast" bean it's too late.
                updateProgress("Initializing Hazelcast", 10);
            }

            return null;
        }

        private void updateProgress(final String message, final int progress) {
            publishProgress(message, progress);
        }
    }

    /**
     * @author Christophe Friederich
     */
    private final class StartupProgressApplicationListener implements ApplicationListener<StartupProgressEvent> {

        @Override
        public void onApplicationEvent(final StartupProgressEvent event) {
            startupManager.onProgress(event.getProgress());
        }
    }
}
