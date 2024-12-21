package com.pmi.tpd.spring.env;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.Ordered;
import org.springframework.core.PriorityOrdered;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.converter.GenericConverter;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;
import org.springframework.core.env.PropertySources;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import com.pmi.tpd.api.config.annotation.ConfigurationProperties;
import com.pmi.tpd.spring.context.bind.PropertiesConfigurationFactory;
import com.pmi.tpd.spring.convert.ApplicationConversionService;

/**
 * {@link BeanPostProcessor} to bind {@link PropertySources} to beans annotated with {@link ConfigurationProperties}.
 *
 * @author Dave Syer
 * @author Phillip Webb
 * @author Christian Dupuis
 * @author Stephane Nicoll
 */
public class ConfigurationPropertiesBindingPostProcessor
        implements BeanPostProcessor, BeanFactoryAware, EnvironmentAware, ApplicationContextAware, InitializingBean,
        DisposableBean, ApplicationListener<ContextRefreshedEvent>, PriorityOrdered {

    /**
     * The bean name of the configuration properties validator.
     */
    public static final String VALIDATOR_BEAN_NAME = "configurationPropertiesValidator";

    /** */
    private static final String[] VALIDATOR_CLASSES = { "javax.validation.Validator",
            "javax.validation.ValidatorFactory" };

    /** */
    private ConfigurationBeanFactoryMetaData beans = new ConfigurationBeanFactoryMetaData();

    /** */
    private PropertySources propertySources;

    /** */
    private Validator validator;

    /** */
    private volatile Validator localValidator;

    /** */
    private ConversionService conversionService;

    /** */
    private ApplicationConversionService defaultConversionService;

    /** */
    private BeanFactory beanFactory;

    /** */
    private Environment environment = new StandardEnvironment();

    /** */
    private ApplicationContext applicationContext;

    /** */
    private List<Converter<?, ?>> converters = Collections.emptyList();

    /** */
    private List<GenericConverter> genericConverters = Collections.emptyList();

    /** */
    private int order = Ordered.HIGHEST_PRECEDENCE + 1;

    /**
     * A list of custom converters (in addition to the defaults) to use when converting properties for binding.
     *
     * @param converters
     *            the converters to set
     */
    @Autowired(required = false)
    @ConfigurationPropertiesBinding
    public void setConverters(final List<Converter<?, ?>> converters) {
        this.converters = converters;
    }

    /**
     * A list of custom converters (in addition to the defaults) to use when converting properties for binding.
     *
     * @param converters
     *            the converters to set
     */
    @Autowired(required = false)
    @ConfigurationPropertiesBinding
    public void setGenericConverters(final List<GenericConverter> converters) {
        this.genericConverters = converters;
    }

    /**
     * Set the order of the bean.
     *
     * @param order
     *            the order
     */
    public void setOrder(final int order) {
        this.order = order;
    }

    /**
     * Return the order of the bean.
     *
     * @return the order
     */
    @Override
    public int getOrder() {
        return this.order;
    }

    /**
     * Set the property sources to bind.
     *
     * @param propertySources
     *            the property sources
     */
    public void setPropertySources(final PropertySources propertySources) {
        this.propertySources = propertySources;
    }

    /**
     * Set the bean validator used to validate property fields.
     *
     * @param validator
     *            the validator
     */
    public void setValidator(final Validator validator) {
        this.validator = validator;
    }

    /**
     * Set the conversion service used to convert property values.
     *
     * @param conversionService
     *            the conversion service
     */
    public void setConversionService(final ConversionService conversionService) {
        this.conversionService = conversionService;
    }

    /**
     * Set the bean meta-data store.
     *
     * @param beans
     *            the bean meta data store
     */
    public void setBeanMetaDataStore(final ConfigurationBeanFactoryMetaData beans) {
        this.beans = beans;
    }

    @Override
    public void setBeanFactory(final BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }

    @Override
    public void setEnvironment(final Environment environment) {
        this.environment = environment;
    }

    @Override
    public void setApplicationContext(final ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        if (this.propertySources == null) {
            this.propertySources = deducePropertySources();
        }
        if (this.validator == null) {
            this.validator = getOptionalBean(VALIDATOR_BEAN_NAME, Validator.class);
        }
        if (this.conversionService == null) {
            this.conversionService = getOptionalBean(ConfigurableApplicationContext.CONVERSION_SERVICE_BEAN_NAME,
                ConversionService.class);
        }
    }

    @Override
    public void onApplicationEvent(final ContextRefreshedEvent event) {
        freeLocalValidator();
    }

    @Override
    public void destroy() throws Exception {
        freeLocalValidator();
    }

    private void freeLocalValidator() {
        try {
            final Validator validator = this.localValidator;
            this.localValidator = null;
            if (validator != null) {
                ((DisposableBean) validator).destroy();
            }
        } catch (final Exception ex) {
            throw new IllegalStateException(ex);
        }
    }

    private PropertySources deducePropertySources() {
        final PropertySourcesPlaceholderConfigurer configurer = getSinglePropertySourcesPlaceholderConfigurer();
        if (configurer != null) {
            // Flatten the sources into a single list so they can be iterated
            return new FlatPropertySources(configurer.getAppliedPropertySources());
        }
        if (this.environment instanceof ConfigurableEnvironment) {
            final MutablePropertySources propertySources = ((ConfigurableEnvironment) this.environment)
                    .getPropertySources();
            return new FlatPropertySources(propertySources);
        }
        // empty, so not very useful, but fulfils the contract
        return new MutablePropertySources();
    }

    private PropertySourcesPlaceholderConfigurer getSinglePropertySourcesPlaceholderConfigurer() {
        // Take care not to cause early instantiation of all FactoryBeans
        if (this.beanFactory instanceof ListableBeanFactory) {
            final ListableBeanFactory listableBeanFactory = (ListableBeanFactory) this.beanFactory;
            final Map<String, PropertySourcesPlaceholderConfigurer> beans = listableBeanFactory
                    .getBeansOfType(PropertySourcesPlaceholderConfigurer.class, false, false);
            if (beans.size() == 1) {
                return beans.values().iterator().next();
            }
        }
        return null;
    }

    private <T> T getOptionalBean(final String name, final Class<T> type) {
        try {
            return this.beanFactory.getBean(name, type);
        } catch (final NoSuchBeanDefinitionException ex) {
            return null;
        }
    }

    @Override
    public Object postProcessBeforeInitialization(final Object bean, final String beanName) throws BeansException {
        ConfigurationProperties annotation = AnnotationUtils.findAnnotation(bean.getClass(),
            ConfigurationProperties.class);
        if (annotation != null) {
            postProcessBeforeInitialization(bean, beanName, annotation);
        } else {
            annotation = this.beans.findFactoryAnnotation(beanName, ConfigurationProperties.class);
            if (annotation != null) {
                postProcessBeforeInitialization(bean, beanName, annotation);
            }
        }
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(final Object bean, final String beanName) throws BeansException {
        return bean;
    }

    private void postProcessBeforeInitialization(final Object bean,
        final String beanName,
        final ConfigurationProperties annotation) {
        final Object target = bean;
        final PropertiesConfigurationFactory<Object> factory = new PropertiesConfigurationFactory<>(target);

        factory.setPropertySources(this.propertySources);
        factory.setValidator(determineValidator(bean));
        // If no explicit conversion service is provided we add one so that (at least)
        // comma-separated arrays of convertibles can be bound automatically
        factory.setConversionService(
            this.conversionService == null ? getDefaultConversionService() : this.conversionService);
        if (annotation != null) {
            factory.setIgnoreInvalidFields(annotation.ignoreInvalidFields());
            factory.setIgnoreUnknownFields(annotation.ignoreUnknownFields());
            factory.setExceptionIfInvalid(annotation.exceptionIfInvalid());
            factory.setIgnoreNestedProperties(annotation.ignoreNestedProperties());
            final String targetName = StringUtils.hasLength(annotation.value()) ? annotation.value()
                    : annotation.prefix();
            if (StringUtils.hasLength(targetName)) {
                factory.setTargetName(targetName);
            }
        }
        try {
            factory.bindPropertiesToTarget();
        } catch (final Exception ex) {
            final String targetClass = ClassUtils.getShortName(target.getClass());
            throw new BeanCreationException(beanName,
                    "Could not bind properties to " + targetClass + " (" + getAnnotationDetails(annotation) + ")", ex);
        }
    }

    private String getAnnotationDetails(final ConfigurationProperties annotation) {
        if (annotation == null) {
            return "";
        }
        final StringBuilder details = new StringBuilder();
        details.append("prefix=")
                .append(StringUtils.hasLength(annotation.value()) ? annotation.value() : annotation.prefix());
        details.append(", ignoreInvalidFields=").append(annotation.ignoreInvalidFields());
        details.append(", ignoreUnknownFields=").append(annotation.ignoreUnknownFields());
        details.append(", ignoreNestedProperties=").append(annotation.ignoreNestedProperties());
        return details.toString();
    }

    private Validator determineValidator(final Object bean) {
        final Validator validator = getValidator();
        final boolean supportsBean = validator != null && validator.supports(bean.getClass());
        if (ClassUtils.isAssignable(Validator.class, bean.getClass())) {
            if (supportsBean) {
                return new ChainingValidator(validator, (Validator) bean);
            }
            return (Validator) bean;
        }
        return supportsBean ? validator : null;
    }

    private Validator getValidator() {
        if (this.validator != null) {
            return this.validator;
        }
        if (this.localValidator == null && isJsr303Present()) {
            this.localValidator = new LocalValidatorFactory().run(this.applicationContext);
        }
        return this.localValidator;
    }

    private boolean isJsr303Present() {
        for (final String validatorClass : VALIDATOR_CLASSES) {
            if (!ClassUtils.isPresent(validatorClass, this.applicationContext.getClassLoader())) {
                return false;
            }
        }
        return true;
    }

    private ConversionService getDefaultConversionService() {
        if (this.defaultConversionService == null) {
            final ApplicationConversionService conversionService = new ApplicationConversionService();
            this.applicationContext.getAutowireCapableBeanFactory().autowireBean(this);
            for (final Converter<?, ?> converter : this.converters) {
                conversionService.addConverter(converter);
            }
            for (final GenericConverter genericConverter : this.genericConverters) {
                conversionService.addConverter(genericConverter);
            }
            this.defaultConversionService = conversionService;
        }
        return this.defaultConversionService;
    }

    /**
     * Factory to create JSR 303 LocalValidatorFactoryBean. Inner class to prevent class loader issues.
     */
    private static class LocalValidatorFactory {

        public Validator run(final ApplicationContext applicationContext) {
            final LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
            validator.setApplicationContext(applicationContext);
            validator.afterPropertiesSet();
            return validator;
        }

    }

    /**
     * {@link Validator} implementation that wraps {@link Validator} instances and chains their execution.
     */
    private static class ChainingValidator implements Validator {

        /** */
        private final Validator[] validators;

        ChainingValidator(final Validator... validators) {
            Assert.notNull(validators, "Validators must not be null");
            this.validators = validators;
        }

        @Override
        public boolean supports(final Class<?> clazz) {
            for (final Validator validator : this.validators) {
                if (validator.supports(clazz)) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public void validate(final Object target, final Errors errors) {
            for (final Validator validator : this.validators) {
                if (validator.supports(target.getClass())) {
                    validator.validate(target, errors);
                }
            }
        }

    }

    /**
     * Convenience class to flatten out a tree of property sources without losing the reference to the backing data
     * (which can therefore be updated in the background).
     */
    private static class FlatPropertySources implements PropertySources {

        /** */
        private final PropertySources propertySources;

        FlatPropertySources(final PropertySources propertySources) {
            this.propertySources = propertySources;
        }

        @Override
        public Iterator<PropertySource<?>> iterator() {
            final MutablePropertySources result = getFlattened();
            return result.iterator();
        }

        @Override
        public boolean contains(final String name) {
            return get(name) != null;
        }

        @Override
        public PropertySource<?> get(final String name) {
            return getFlattened().get(name);
        }

        private MutablePropertySources getFlattened() {
            final MutablePropertySources result = new MutablePropertySources();
            for (final PropertySource<?> propertySource : this.propertySources) {
                flattenPropertySources(propertySource, result);
            }
            return result;
        }

        private void flattenPropertySources(final PropertySource<?> propertySource,
            final MutablePropertySources result) {
            final Object source = propertySource.getSource();
            if (source instanceof ConfigurableEnvironment) {
                final ConfigurableEnvironment environment = (ConfigurableEnvironment) source;
                for (final PropertySource<?> childSource : environment.getPropertySources()) {
                    flattenPropertySources(childSource, result);
                }
            } else {
                result.addLast(propertySource);
            }
        }

    }

}
