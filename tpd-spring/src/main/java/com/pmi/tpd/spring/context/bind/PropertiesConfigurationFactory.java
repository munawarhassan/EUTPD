package com.pmi.tpd.spring.context.bind;

import java.beans.PropertyDescriptor;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.PropertyValues;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceAware;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.env.PropertySources;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.DataBinder;
import org.springframework.validation.ObjectError;
import org.springframework.validation.Validator;

import com.pmi.tpd.api.util.Assert;
import com.pmi.tpd.spring.context.RelaxedNames;

/**
 * Validate some {@link Properties} (or optionally {@link PropertySources}) by binding them to an object of a specified
 * type and then optionally running a {@link Validator} over it.
 *
 * @param <T>
 *            The target type
 * @author Dave Syer
 */
public class PropertiesConfigurationFactory<T> implements FactoryBean<T>, MessageSourceAware, InitializingBean {

    /** */
    private static final char[] EXACT_DELIMITERS = { '_', '.', '[' };

    /** */
    private static final char[] TARGET_NAME_DELIMITERS = { '_', '.' };

    /** */
    private final Log logger = LogFactory.getLog(getClass());

    /** */
    private boolean ignoreUnknownFields = true;

    /** */
    private boolean ignoreInvalidFields;

    /** */
    private boolean exceptionIfInvalid = true;

    /** */
    private Properties properties;

    /** */
    private PropertySources propertySources;

    /** */
    private final T target;

    /** */
    private Validator validator;

    /** */
    private MessageSource messageSource;

    /** */
    private boolean hasBeenBound = false;

    /** */
    private boolean ignoreNestedProperties = false;

    /** */
    private String targetName;

    /** */
    private ConversionService conversionService;

    /**
     * Create a new {@link PropertiesConfigurationFactory} instance.
     *
     * @param target
     *            the target object to bind too
     * @see #PropertiesConfigurationFactory(Class)
     */
    public PropertiesConfigurationFactory(final T target) {
        this.target = Assert.checkNotNull(target, "target");
    }

    /**
     * Create a new {@link PropertiesConfigurationFactory} instance.
     *
     * @param type
     *            the target type
     * @see #PropertiesConfigurationFactory(Class)
     */
    @SuppressWarnings("unchecked")
    public PropertiesConfigurationFactory(final Class<?> type) {
        Assert.notNull(type);
        this.target = (T) BeanUtils.instantiateClass(type);
    }

    /**
     * Flag to disable binding of nested properties (i.e. those with period separators in their paths). Can be useful to
     * disable this if the name prefix is empty and you don't want to ignore unknown fields.
     *
     * @param ignoreNestedProperties
     *            the flag to set (default false)
     */
    public void setIgnoreNestedProperties(final boolean ignoreNestedProperties) {
        this.ignoreNestedProperties = ignoreNestedProperties;
    }

    /**
     * Set whether to ignore unknown fields, that is, whether to ignore bind parameters that do not have corresponding
     * fields in the target object.
     * <p>
     * Default is "true". Turn this off to enforce that all bind parameters must have a matching field in the target
     * object.
     *
     * @param ignoreUnknownFields
     *            if unknown fields should be ignored
     */
    public void setIgnoreUnknownFields(final boolean ignoreUnknownFields) {
        this.ignoreUnknownFields = ignoreUnknownFields;
    }

    /**
     * Set whether to ignore invalid fields, that is, whether to ignore bind parameters that have corresponding fields
     * in the target object which are not accessible (for example because of null values in the nested path).
     * <p>
     * Default is "false". Turn this on to ignore bind parameters for nested objects in non-existing parts of the target
     * object graph.
     *
     * @param ignoreInvalidFields
     *            if invalid fields should be ignored
     */
    public void setIgnoreInvalidFields(final boolean ignoreInvalidFields) {
        this.ignoreInvalidFields = ignoreInvalidFields;
    }

    /**
     * Set the target name.
     *
     * @param targetName
     *            the target name
     */
    public void setTargetName(final String targetName) {
        this.targetName = targetName;
    }

    /**
     * Set the message source.
     *
     * @param messageSource
     *            the message source
     */
    @Override
    public void setMessageSource(final MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    /**
     * Set the properties.
     *
     * @param properties
     *            the properties
     */
    public void setProperties(final Properties properties) {
        this.properties = properties;
    }

    /**
     * Set the property sources.
     *
     * @param propertySources
     *            the property sources
     */
    public void setPropertySources(final PropertySources propertySources) {
        this.propertySources = propertySources;
    }

    /**
     * Set the conversion service.
     *
     * @param conversionService
     *            the conversion service
     */
    public void setConversionService(final ConversionService conversionService) {
        this.conversionService = conversionService;
    }

    /**
     * Set the validator.
     *
     * @param validator
     *            the validator
     */
    public void setValidator(final Validator validator) {
        this.validator = validator;
    }

    /**
     * Set a flag to indicate that an exception should be raised if a Validator is available and validation fails.
     *
     * @param exceptionIfInvalid
     *            the flag to set
     */
    public void setExceptionIfInvalid(final boolean exceptionIfInvalid) {
        this.exceptionIfInvalid = exceptionIfInvalid;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        bindPropertiesToTarget();
    }

    @Override
    public Class<?> getObjectType() {
        if (this.target == null) {
            return Object.class;
        }
        return this.target.getClass();
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    @Override
    public T getObject() throws Exception {
        if (!this.hasBeenBound) {
            bindPropertiesToTarget();
        }
        return this.target;
    }

    /**
     * @throws BindException
     *             if binding validation errors occur.
     */
    public void bindPropertiesToTarget() throws BindException {
        Assert.isTrue(this.properties != null || this.propertySources != null,
            "Properties or propertySources should not be null");
        try {
            if (this.logger.isTraceEnabled()) {
                if (this.properties != null) {
                    this.logger.trace(String.format("Properties:%n%s" + this.properties));
                } else {
                    this.logger.trace("Property Sources: " + this.propertySources);
                }
            }
            this.hasBeenBound = true;
            doBindPropertiesToTarget();
        } catch (final BindException ex) {
            if (this.exceptionIfInvalid) {
                throw ex;
            }
            this.logger.error("Failed to load Properties validation bean. " + "Your Properties may be invalid.", ex);
        }
    }

    private void doBindPropertiesToTarget() throws BindException {
        final RelaxedDataBinder dataBinder = this.targetName != null
                ? new RelaxedDataBinder(this.target, this.targetName) : new RelaxedDataBinder(this.target);
        if (this.validator != null) {
            dataBinder.setValidator(this.validator);
        }
        if (this.conversionService != null) {
            dataBinder.setConversionService(this.conversionService);
        }
        dataBinder.setIgnoreNestedProperties(this.ignoreNestedProperties);
        dataBinder.setIgnoreInvalidFields(this.ignoreInvalidFields);
        dataBinder.setIgnoreUnknownFields(this.ignoreUnknownFields);
        customizeBinder(dataBinder);
        final Iterable<String> relaxedTargetNames = getRelaxedTargetNames();
        final Set<String> names = getNames(relaxedTargetNames);
        final PropertyValues propertyValues = getPropertyValues(names, relaxedTargetNames);
        dataBinder.bind(propertyValues);
        if (this.validator != null) {
            validate(dataBinder);
        }
    }

    private Iterable<String> getRelaxedTargetNames() {
        return this.target != null && StringUtils.hasLength(this.targetName) ? new RelaxedNames(this.targetName) : null;
    }

    private Set<String> getNames(final Iterable<String> prefixes) {
        final Set<String> names = new LinkedHashSet<>();
        if (this.target != null) {
            final PropertyDescriptor[] descriptors = BeanUtils.getPropertyDescriptors(this.target.getClass());
            for (final PropertyDescriptor descriptor : descriptors) {
                final String name = descriptor.getName();
                if (!name.equals("class")) {
                    final RelaxedNames relaxedNames = RelaxedNames.forCamelCase(name);
                    if (prefixes == null) {
                        for (final String relaxedName : relaxedNames) {
                            names.add(relaxedName);
                        }
                    } else {
                        for (final String prefix : prefixes) {
                            for (final String relaxedName : relaxedNames) {
                                names.add(prefix + "." + relaxedName);
                                names.add(prefix + "_" + relaxedName);
                            }
                        }
                    }
                }
            }
        }
        return names;
    }

    private PropertyValues getPropertyValues(final Set<String> names, final Iterable<String> relaxedTargetNames) {
        if (this.properties != null) {
            return new MutablePropertyValues(this.properties);
        }
        return getPropertySourcesPropertyValues(names, relaxedTargetNames);
    }

    private PropertyValues getPropertySourcesPropertyValues(final Set<String> names,
        final Iterable<String> relaxedTargetNames) {
        final PropertyNamePatternsMatcher includes = getPropertyNamePatternsMatcher(names, relaxedTargetNames);
        return new PropertySourcesPropertyValues(this.propertySources, names, includes);
    }

    private PropertyNamePatternsMatcher getPropertyNamePatternsMatcher(final Set<String> names,
        final Iterable<String> relaxedTargetNames) {
        if (this.ignoreUnknownFields && !isMapTarget()) {
            // Since unknown fields are ignored we can filter them out early to save
            // unnecessary calls to the PropertySource.
            return new DefaultPropertyNamePatternsMatcher(EXACT_DELIMITERS, true, names);
        }
        if (relaxedTargetNames != null) {
            // We can filter properties to those starting with the target name, but
            // we can't do a complete filter since we need to trigger the
            // unknown fields check
            final Set<String> relaxedNames = new HashSet<>();
            for (final String relaxedTargetName : relaxedTargetNames) {
                relaxedNames.add(relaxedTargetName);
            }
            return new DefaultPropertyNamePatternsMatcher(TARGET_NAME_DELIMITERS, true, relaxedNames);
        }
        // Not ideal, we basically can't filter anything
        return PropertyNamePatternsMatcher.ALL;
    }

    private boolean isMapTarget() {
        return this.target != null && Map.class.isAssignableFrom(this.target.getClass());
    }

    private void validate(final RelaxedDataBinder dataBinder) throws BindException {
        dataBinder.validate();
        final BindingResult errors = dataBinder.getBindingResult();
        if (errors.hasErrors()) {
            this.logger.error("Properties configuration failed validation");
            for (final ObjectError error : errors.getAllErrors()) {
                this.logger.error(this.messageSource != null
                        ? this.messageSource.getMessage(error, Locale.getDefault()) + " (" + error + ")" : error);
            }
            if (this.exceptionIfInvalid) {
                throw new BindException(errors);
            }
        }
    }

    /**
     * Customize the data binder.
     *
     * @param dataBinder
     *            the data binder that will be used to bind and validate
     */
    protected void customizeBinder(final DataBinder dataBinder) {
    }

}
