package com.pmi.tpd.service.testing.cluster;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.AutowiredAnnotationBeanPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.CommonAnnotationBeanPostProcessor;
import org.springframework.context.support.GenericApplicationContext;

import com.hazelcast.spring.context.SpringManagedContext;

public class ResettableSpringManagedContext extends SpringManagedContext {

    private GenericApplicationContext applicationContext;

    public ResettableSpringManagedContext() {
        initApplicationContext();
    }

    public void addBeans(final Iterable<Object> beans) {
        if (beans == null) {
            return;
        }
        final ConfigurableListableBeanFactory beanFactory = this.applicationContext.getBeanFactory();
        for (final Object bean : beans) {
            beanFactory.registerSingleton(bean.getClass().getName(), bean);
        }
    }

    public void destroy() {
        if (this.applicationContext == null) {
            return;
        }
        this.applicationContext.close();
        this.applicationContext = null;
    }

    public void reset() {
        initApplicationContext();
    }

    @Override
    public void setApplicationContext(final ApplicationContext applicationContext) throws BeansException {
        throw new UnsupportedOperationException("This version of SpringManagedContext creates its own context");
    }

    private void initApplicationContext() {
        destroy();

        this.applicationContext = new GenericApplicationContext();
        final ConfigurableListableBeanFactory beanFactory = this.applicationContext.getBeanFactory();

        final AutowiredAnnotationBeanPostProcessor autowiredProcessor = new AutowiredAnnotationBeanPostProcessor();
        autowiredProcessor.setBeanFactory(beanFactory);
        final CommonAnnotationBeanPostProcessor commonAnnotationProcessor = new CommonAnnotationBeanPostProcessor();
        commonAnnotationProcessor.setBeanFactory(beanFactory);

        beanFactory.addBeanPostProcessor(commonAnnotationProcessor);
        beanFactory.addBeanPostProcessor(autowiredProcessor);

        this.applicationContext.refresh();

        super.setApplicationContext(this.applicationContext);
    }
}
