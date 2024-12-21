package com.pmi.tpd.core.event.publisher.spring;

import java.security.AccessControlContext;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.Aware;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.support.MergedBeanDefinitionPostProcessor;
import org.springframework.beans.factory.support.RootBeanDefinition;

import com.google.common.collect.ImmutableList;
import com.pmi.tpd.api.event.publisher.IEventPublisher;
import com.pmi.tpd.core.event.config.IEventThreadPoolConfiguration;
import com.pmi.tpd.core.event.config.IListenerHandlersConfiguration;
import com.pmi.tpd.core.event.publisher.AsynchronousAbleEventDispatcher;
import com.pmi.tpd.core.event.publisher.EventExecutorFactoryImpl;
import com.pmi.tpd.core.event.publisher.EventPublisherImpl;
import com.pmi.tpd.core.event.publisher.EventThreadPoolConfigurationImpl;
import com.pmi.tpd.core.event.publisher.IEventDispatcher;
import com.pmi.tpd.core.event.publisher.IEventExecutorFactory;
import com.pmi.tpd.core.event.publisher.IEventPublisherAware;
import com.pmi.tpd.core.event.publisher.IListenerHandler;
import com.pmi.tpd.core.event.publisher.ListenerHandlerConfigurationImpl;
import com.pmi.tpd.core.event.publisher.LockFreeEventPublisher;
import com.pmi.tpd.core.event.publisher.TransactionAwareEventPublisher;
import com.pmi.tpd.spring.transaction.ITransactionSynchronizer;

/**
 * * {@link org.springframework.beans.factory.FactoryBean} that creates a named {@link IEventPublisher} instance.
 *
 * @see IListenerHandlersConfiguration
 * @see com.pmi.tpd.core.event.publisher.IEventDispatcher
 * @see com.pmi.tpd.api.event.annotation.AsynchronousPreferred
 * @see com.pmi.tpd.api.event.annotation.EventListener
 * @author Christophe Friederich
 * @since 1.0
 */
public class EventPublisherFactoryBean
        implements FactoryBean<IEventPublisher>, InitializingBean, BeanDefinitionRegistryPostProcessor {

    /**
    *
    */
    private final Map<String, Boolean> singletonNames = new ConcurrentHashMap<>();

    /**
     * Specific {@link IListenerHandlersConfiguration}.
     */
    private final IListenerHandlersConfiguration listenerHandlers = new ListenerHandlerConfigurationImpl();

    /**
     * log instance.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(EventPublisherFactoryBean.class);

    /**
     * {@link IEventPublisher} instance exposed by {@link FactoryBean} interface.
     */
    private IEventPublisher eventPublisher;

    private final ITransactionSynchronizer synchronizer;

    /**
     * indicate the synchronisation between each event type dispatching.
     */
    private boolean blockingDispatch = true;

    /**
     * Execution management.
     *
     * @see org.springframework.scheduling.concurrent.ThreadPoolExecutorFactoryBean
     */
    private ExecutorService executorService;

    public EventPublisherFactoryBean(@Nullable final ITransactionSynchronizer synchronizer) {
        this.synchronizer = synchronizer;
    }

    /**
     * Execution management.
     *
     * @see org.springframework.scheduling.concurrent.ThreadPoolExecutorFactoryBean
     */
    public EventPublisherFactoryBean() {
        this(null);
    }

    /**
     * This method allows the bean instance to perform initialisation only possible when all bean properties have been
     * set and to throw an exception in the event of misconfiguration.
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        IEventExecutorFactory executorFactory = null;
        if (this.executorService == null) {
            // Use default Thread pool configuration
            final IEventThreadPoolConfiguration threadPoolConfiguration = new EventThreadPoolConfigurationImpl();
            executorFactory = new EventExecutorFactoryImpl(threadPoolConfiguration);
        } else {
            executorFactory = new EventExecutorProvider(this.executorService);
        }
        final IEventDispatcher eventDispatcher = new AsynchronousAbleEventDispatcher(executorFactory);
        if (!isBlockingDispatch()) {
            eventPublisher = new LockFreeEventPublisher(eventDispatcher, listenerHandlers);
        } else {
            eventPublisher = new EventPublisherImpl(eventDispatcher, listenerHandlers);
            if (synchronizer != null) {
                eventPublisher = new TransactionAwareEventPublisher(eventPublisher, synchronizer);
            }
        }
    };

    @PreDestroy
    public void destroy() {
        if (eventPublisher != null) {
            this.eventPublisher.shutdown();
        }
    }

    /** {@inheritDoc} */
    @Override
    @Nonnull
    public IEventPublisher getObject() throws Exception {
        return eventPublisher;
    }

    /** {@inheritDoc} */
    @Override
    @Nonnull
    public Class<?> getObjectType() {
        if (this.eventPublisher == null) {
            return IEventPublisher.class;
        }
        return this.eventPublisher.getClass();
    }

    /** {@inheritDoc} */
    @Override
    public boolean isSingleton() {
        return true;
    }

    /**
     * <p>
     * Getter for the field <code>listenerHandlers</code>.
     * </p>
     *
     * @return Returns list of {@link IListenerHandler}. If no listener handler exists returns empty list.
     * @see IListenerHandler
     */
    @Nonnull
    protected List<IListenerHandler> getListenerHandlers() {
        if (listenerHandlers == null) {
            return ImmutableList.of();
        }
        return listenerHandlers.getListenerHandlers();
    }

    /**
     * Sets the indicating whether the event 'is' dispatched asynchronously or not.
     * <p>
     * Default is {@code true}.
     * </p>
     *
     * @param blocking
     *            allows dispatch event asynchronous or lock.
     * @see com.pmi.tpd.api.event.annotation.AsynchronousPreferred
     */
    public void setBlockingDispactch(final boolean blocking) {
        this.blockingDispatch = blocking;
    }

    /**
     * Gets indicating whether the event 'is' dispatched asynchronously or not.
     * <p>
     * Default is {@code true}.
     * </p>
     *
     * @return Returns <code>true</code> whether the event 'is' dispatched synchronously, otherwise <code>false</code>.
     */
    public boolean isBlockingDispatch() {
        return blockingDispatch;
    }

    /**
     * Sets the executor service.
     * <p>
     * <strong>Note: </strong> to set before call initialisation {@link #afterPropertiesSet()} method.
     * </p>
     *
     * @param executorService
     *            executor service
     */
    public void setExecutorService(final ExecutorService executorService) {
        this.executorService = executorService;
    }

    /**
     * Gets the associated Logger.
     *
     * @return Returns {@link org.slf4j.Logger} associated.
     */
    public Logger getLogger() {
        return LOGGER;
    }

    /** {@inheritDoc} */
    @Override
    public void postProcessBeanDefinitionRegistry(final BeanDefinitionRegistry registry) {
        // not use
    }

    /** {@inheritDoc} */
    @Override
    public void postProcessBeanFactory(final ConfigurableListableBeanFactory beanFactory) {
        // register Post processor
        beanFactory.addBeanPostProcessor(new EventBeanPostProcessor());
        beanFactory.addBeanPostProcessor(new EventListenerAwareProcessor(beanFactory));
    }

    /**
     * @author devacfr
     */
    class EventBeanPostProcessor implements MergedBeanDefinitionPostProcessor, BeanFactoryAware {

        /**
         *
         */
        private BeanFactory beanFactory;

        /**
         * {@inheritDoc}
         */
        @Override
        public void setBeanFactory(final BeanFactory beanFactory) {
            this.beanFactory = beanFactory;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void postProcessMergedBeanDefinition(final RootBeanDefinition beanDefinition,
            final Class<?> beanType,
            final String beanName) {
            if (beanDefinition.isSingleton()) {
                singletonNames.put(beanName, Boolean.TRUE);
            }
        }

        @Override
        public Object postProcessBeforeInitialization(final Object bean, final String beanName) {
            return bean;
        }

        @Override
        public Object postProcessAfterInitialization(final Object bean, final String beanName) {
            if (isHandler(bean)) {
                // potentially not detected as a listener by getBeanNamesForType retrieval
                final Boolean flag = singletonNames.get(beanName);
                if (Boolean.TRUE.equals(flag)) {
                    // singleton bean (top-level or inner): register on the fly
                    eventPublisher.register(bean);
                } else if (flag == null) {
                    if (LOGGER.isWarnEnabled() && !beanFactory.containsBean(beanName)) {
                        // inner bean with other scope - can't reliably process events
                        LOGGER.warn("Inner bean '" + beanName + "' implements ApplicationListener interface "
                                + "but is not reachable for event multicasting by its containing ApplicationContext "
                                + "because it does not have singleton scope. Only top-level listener beans are allowed "
                                + "to be of non-singleton scope.");
                    }
                    singletonNames.put(beanName, Boolean.FALSE);
                }
            }
            return bean;
        }

        /**
         * Gets the indicating whether the bean supports at one least {@link ISupportedListenerHandler}.
         *
         * @param bean
         *            bean to test
         * @return Returns <code>true</code> whether the bean supports at one least {@link ISupportedListenerHandler},
         *         otherwise returns <code>false</code>.
         */
        private boolean isHandler(final Object bean) {
            for (final IListenerHandler handler : getListenerHandlers()) {
                try {
                    if (handler.supportsHandler(bean)) {
                        return true;
                    }
                } catch (final RuntimeException ex) {
                    LOGGER.warn(ex.getMessage());
                }

            }
            return false;
        }

    }

    /**
     * {@link org.springframework.beans.factory.config.BeanPostProcessor} implementation that passes the EventPublisher
     * to beans that implement the {@link EventListenerAwareProcessor} interface.
     *
     * @author Christophe Friederich
     */
    class EventListenerAwareProcessor implements BeanPostProcessor {

        /**
         * Spring factory bean.
         */
        private final ConfigurableListableBeanFactory beanFactory;

        /**
         * Create a new ApplicationContextAwareProcessor for the given context.
         *
         * @param applicationContext
         *            Spring factory bean
         */
        EventListenerAwareProcessor(final ConfigurableListableBeanFactory applicationContext) {
            this.beanFactory = applicationContext;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Object postProcessBeforeInitialization(final Object bean, final String beanName) {
            AccessControlContext acc = null;

            if (System.getSecurityManager() != null && bean instanceof IEventPublisherAware) {
                acc = this.beanFactory.getAccessControlContext();
            }

            if (acc != null) {
                AccessController.doPrivileged((PrivilegedAction<Object>) () -> {
                    invokeAwareInterfaces(bean);
                    return null;
                }, acc);
            } else {
                invokeAwareInterfaces(bean);
            }

            return bean;
        }

        /**
         * Invoke {@link IEventPublisherAware#setEventPublisher(EventPublisher)} on bean implementing
         * {@link IEventPublisherAware} interface.
         *
         * @param bean
         *            Object to invoke
         */
        private void invokeAwareInterfaces(final Object bean) {
            if (bean instanceof Aware) {
                if (bean instanceof IEventPublisherAware) {
                    ((IEventPublisherAware) bean).setEventPublisher(eventPublisher);
                }
            }
        }

        @Override
        public Object postProcessAfterInitialization(final Object bean, final String beanName) {
            return bean;
        }

    }
}
