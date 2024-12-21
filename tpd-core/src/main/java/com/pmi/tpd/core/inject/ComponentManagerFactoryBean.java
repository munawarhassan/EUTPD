package com.pmi.tpd.core.inject;

import static com.pmi.tpd.api.util.Assert.checkHasText;
import static com.pmi.tpd.api.util.Assert.checkNotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.SmartFactoryBean;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionReaderUtils;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;

import com.google.common.collect.Sets;
import com.pmi.tpd.api.exception.InfrastructureException;

/**
 * <p>
 * ComponentManagerFactoryBean class.
 * </p>
 *
 * @author Christophe Friederich
 * @since 1.0
 */
public class ComponentManagerFactoryBean
        implements BeanFactoryAware, SmartFactoryBean<IComponentManager>, DisposableBean {

    /** */
    private BeanFactory beanFactory;

    /** */
    private static final SpringFactory FACTORY = new SpringFactory();

    /**
     * <p>
     * Getter for the field <code>instance</code>.
     * </p>
     *
     * @return a {@link org.devacfr.core.inject.IComponentManager} object.
     */
    public static IComponentManager.Factory getInjector() {
        return FACTORY;
    }

    /** {@inheritDoc} */
    @Override
    public final void destroy() throws Exception {
        doDestroy();
        try {
            FACTORY.destroy();
        } catch (final Exception ex) {

        }
        beanFactory = null;
    }

    /**
     * <p>
     * doDestroy.
     * </p>
     */
    protected void doDestroy() {

    }

    /** {@inheritDoc} */
    @Override
    public IComponentManager getObject() throws Exception {
        return getInjector();
    }

    /** {@inheritDoc} */
    @Override
    public Class<IComponentManager> getObjectType() {
        return IComponentManager.class;
    }

    /** {@inheritDoc} */
    @Override
    public boolean isSingleton() {
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public boolean isEagerInit() {
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public boolean isPrototype() {
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public void setBeanFactory(final BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
        FACTORY.setBeanFactory(new DefaultListableBeanFactory(this.beanFactory));
    }

    /**
     * @author Christophe Friederich
     * @since 1.0
     */
    public static class SpringFactory extends IComponentManager.Factory {

        /** */
        private DefaultListableBeanFactory beanFactory;

        /**
         * Invoked by Dependency injection on destruction of a singleton.
         *
         * @throws Exception
         *             in case of shutdown errors. Exceptions will get logged but not rethrown to allow other beans to
         *             release their resources too.
         */
        public void destroy() throws Exception {
            if (beanFactory != null) {
                beanFactory.destroySingletons();
            }
            beanFactory = null;
        }

        /**
         * @param beanFactory
         *            a bean Spring factory
         */
        public void setBeanFactory(final DefaultListableBeanFactory beanFactory) {
            this.beanFactory = beanFactory;
        }

        /** {@inheritDoc} */
        @Override
        @Nonnull
        public <T> T getComponentInstanceOfType(@Nonnull final Class<T> requiredType) {
            checkInitialized();
            return beanFactory.getBean(checkNotNull(requiredType, "requiredType"));
        }

        /** {@inheritDoc} */
        @Override
        @Nonnull
        public <T> T getComponentInstanceOfType(@Nullable final Class<T> clazz, @Nonnull final String name) {
            checkInitialized();
            return beanFactory.getBean(checkHasText(name, "name"), clazz);
        }

        /** {@inheritDoc} */
        @SuppressWarnings("unchecked")
        @Override
        @Nonnull
        public <T> List<T> getComponentInstancesOfType(@Nullable final Class<T> clazz) {
            checkInitialized();
            final Set<String> beanNames = Sets.newHashSet();
            beanNames.addAll(Arrays.asList(beanFactory.getBeanNamesForType(clazz)));
            if (beanFactory.getParentBeanFactory() != null) {
                beanNames.addAll(Arrays
                        .asList(((ListableBeanFactory) beanFactory.getParentBeanFactory()).getBeanNamesForType(clazz)));
            }
            final List<T> l = new ArrayList<>(beanNames.size());
            for (final String name : beanNames) {
                l.add((T) beanFactory.getBean(name));
            }

            return l;
        }

        /**
         * @throws InfrastructureException
         */
        private void checkInitialized() throws InfrastructureException {
            if (this.beanFactory == null) {
                throw new InfrastructureException("Spring has not started yet");
            }
        }

        /** {@inheritDoc} */
        @Override
        @Nonnull
        public <T> T registerSingletonComponentImplementation(@Nonnull final Class<T> clazz) {
            return registerComponentImplementation(checkNotNull(clazz, "clazz"), clazz.getName(), Scope.Singleton);
        }

        /** {@inheritDoc} */
        @Override
        @Nonnull
        public <T> T registerSingletonComponentImplementation(@Nonnull final Class<T> clazz,
            @Nonnull final String name) {
            return registerComponentImplementation(checkNotNull(clazz, "clazz"),
                checkHasText(name, "name"),
                Scope.Singleton);
        }

        /** {@inheritDoc} */
        @Override
        @Nonnull
        public <T> T registerComponentImplementation(@Nonnull final Class<T> clazz, @Nullable final Scope scope) {
            return registerComponentImplementation(checkNotNull(clazz, "clazz"), clazz.getName(), scope);

        }

        /** {@inheritDoc} */
        @Override
        @Nonnull
        public <T> T registerComponent(@Nonnull final T bean, @Nonnull final String name) {
            checkInitialized();
            checkHasText(name, "name");
            @SuppressWarnings("unchecked")
            final Class<T> clazz = (Class<T>) checkNotNull(bean, "bean").getClass();
            final String className = clazz.getCanonicalName();
            BeanDefinition beanDefinition = null;
            try {
                beanDefinition = BeanDefinitionReaderUtils
                        .createBeanDefinition(null, className, clazz.getClassLoader());
            } catch (final ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
            beanDefinition.setScope(BeanDefinition.SCOPE_SINGLETON);
            beanFactory.registerBeanDefinition(name, beanDefinition);
            beanFactory.registerSingleton(name, bean);
            return bean;
        }

        /** {@inheritDoc} */
        @SuppressWarnings("unchecked")
        @Override
        @Nonnull
        public <T> T registerComponentImplementation(@Nonnull final Class<T> clazz,
            @Nonnull final String name,
            @Nullable final Scope scope) {
            checkInitialized();
            checkHasText(name, "name");
            final String className = checkNotNull(clazz, "clazz").getCanonicalName();
            // create the bean definition
            BeanDefinition beanDefinition = null;
            try {
                beanDefinition = BeanDefinitionReaderUtils
                        .createBeanDefinition(null, className, clazz.getClassLoader());
            } catch (final ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
            if (Scope.Singleton.equals(scope)) {
                beanDefinition.setScope(BeanDefinition.SCOPE_SINGLETON);
            } else {
                beanDefinition.setScope(BeanDefinition.SCOPE_PROTOTYPE);
            }

            // Create the bean - I'm using the class name as the bean name
            beanFactory.registerBeanDefinition(name, beanDefinition);
            T bean = null;
            if (beanDefinition.getScope() == BeanDefinition.SCOPE_SINGLETON) {
                bean = beanFactory.getBean(name, clazz);
            } else {
                bean = (T) beanFactory.createBean(clazz, AutowireCapableBeanFactory.AUTOWIRE_BY_NAME, false);
            }

            return bean;
        }

    }
}
