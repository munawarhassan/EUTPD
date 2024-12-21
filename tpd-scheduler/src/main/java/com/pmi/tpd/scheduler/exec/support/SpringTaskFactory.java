package com.pmi.tpd.scheduler.exec.support;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.util.StringValueResolver;

import com.pmi.tpd.api.scheduler.ITaskFactory;
import com.pmi.tpd.api.util.Assert;
import com.pmi.tpd.scheduler.exec.AbstractRunnableTask;

/**
 * @author Christophe Friederich
 * @since 1.3
 */
public abstract class SpringTaskFactory implements ITaskFactory {

    /** */
    private final ConfigurableBeanFactory beanFactory;

    /** */
    private final StringValueResolver stringValueResolver;

    @Inject
    public SpringTaskFactory(final ApplicationContext applicationContext) {
        this.beanFactory = (ConfigurableBeanFactory) applicationContext.getAutowireCapableBeanFactory();

        this.stringValueResolver = s -> {
            beanFactory.resolveEmbeddedValue(s);
            return s;
        };
    }

    @Override
    public void injectMembers(final Object obj) {
        ((AutowireCapableBeanFactory) beanFactory).autowireBean(obj);

    }

    @Override
    public @Nonnull <T> T createInstance(final Class<T> clazz) {
        return create(clazz, null, null);
    }

    @Override
    public <T> T getInstance(final Class<T> requiredType) {
        return beanFactory.getBean(requiredType);
    }

    @Override
    public <T> T getBean(final Class<T> requiredType, final String name) {
        return beanFactory.getBean(name, requiredType);
    }

    /**
     * @param clazz
     * @param contextBeanName
     * @param context
     * @return
     */
    protected @Nonnull <T> T create(final Class<T> clazz, final String contextBeanName, final Object context) {
        final DefaultListableBeanFactory factory = new DefaultListableBeanFactory(beanFactory);
        factory.copyConfigurationFrom(beanFactory);
        factory.addEmbeddedValueResolver(stringValueResolver);

        // The app context is not available from the child context
        if (context != null) {
            Assert.isTrue(contextBeanName != null, "no context bean name was provided");
            factory.registerSingleton(contextBeanName, context);
        }

        final T obj = clazz.cast(factory.autowire(clazz, AutowireCapableBeanFactory.AUTOWIRE_CONSTRUCTOR, false));
        // TODO find better solution
        if (obj instanceof AbstractRunnableTask) {
            ((AbstractRunnableTask) obj).setTaskFactory(this);
        }
        return obj;
    }

}
