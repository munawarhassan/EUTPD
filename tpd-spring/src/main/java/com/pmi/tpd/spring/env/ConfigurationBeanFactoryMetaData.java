package com.pmi.tpd.spring.env;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.ReflectionUtils.MethodCallback;

/**
 * Utility class to memorize {@code @Bean} definition meta data during initialization of the bean factory.
 *
 * @author Dave Syer
 * @since 1.1.0
 */
public class ConfigurationBeanFactoryMetaData implements BeanFactoryPostProcessor {

    private ConfigurableListableBeanFactory beanFactory;

    private final Map<String, MetaData> beans = new HashMap<String, MetaData>();

    @Override
    public void postProcessBeanFactory(final ConfigurableListableBeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
        for (final String name : beanFactory.getBeanDefinitionNames()) {
            final BeanDefinition definition = beanFactory.getBeanDefinition(name);
            final String method = definition.getFactoryMethodName();
            final String bean = definition.getFactoryBeanName();
            if (method != null && bean != null) {
                this.beans.put(name, new MetaData(bean, method));
            }
        }
    }

    public <A extends Annotation> Map<String, Object> getBeansWithFactoryAnnotation(final Class<A> type) {
        final Map<String, Object> result = new HashMap<String, Object>();
        for (final String name : this.beans.keySet()) {
            if (findFactoryAnnotation(name, type) != null) {
                result.put(name, this.beanFactory.getBean(name));
            }
        }
        return result;
    }

    public <A extends Annotation> A findFactoryAnnotation(final String beanName, final Class<A> type) {
        final Method method = findFactoryMethod(beanName);
        return method == null ? null : AnnotationUtils.findAnnotation(method, type);
    }

    private Method findFactoryMethod(final String beanName) {
        if (!this.beans.containsKey(beanName)) {
            return null;
        }
        final AtomicReference<Method> found = new AtomicReference<Method>(null);
        final MetaData meta = this.beans.get(beanName);
        final String factory = meta.getMethod();
        final Class<?> type = this.beanFactory.getType(meta.getBean());
        ReflectionUtils.doWithMethods(type, new MethodCallback() {

            @Override
            public void doWith(final Method method) throws IllegalArgumentException, IllegalAccessException {
                if (method.getName().equals(factory)) {
                    found.compareAndSet(null, method);
                }
            }
        });
        return found.get();
    }

    private static class MetaData {

        private final String bean;

        private final String method;

        MetaData(final String bean, final String method) {
            this.bean = bean;
            this.method = method;
        }

        public String getBean() {
            return this.bean;
        }

        public String getMethod() {
            return this.method;
        }

    }

}
