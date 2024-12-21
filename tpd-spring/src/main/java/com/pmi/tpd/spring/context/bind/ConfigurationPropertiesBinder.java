package com.pmi.tpd.spring.context.bind;

import static com.pmi.tpd.api.util.Assert.checkNotNull;

import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.PropertyAccessor;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Lists;
import com.pmi.tpd.api.config.annotation.ConfigurationProperties;
import com.pmi.tpd.api.config.annotation.NoPersistent;
import com.pmi.tpd.api.util.Assert;
import com.pmi.tpd.spring.env.ConfigurationPropertiesBindingPostProcessor;
import com.pmi.tpd.spring.env.ConfigurationPropertiesBindingPostProcessorRegistrar;

/**
 * Binder of Configuration properties
 *
 * @author Christophe Friederich
 * @since 2.2
 * @see ConfigurationProperties
 */
public class ConfigurationPropertiesBinder {

  /** */
  private final BeanFactory beanFactory;

  /**
   * @param properties
   * @param applicationProperties
   */
  @Inject
  public ConfigurationPropertiesBinder(@Nonnull final BeanFactory beanFactory) {
    this.beanFactory = checkNotNull(beanFactory, "beanFactory");
  }

  public String getTargetName(@Nonnull final Class<?> configurationClass) {
    final ConfigurationProperties annotation = configurationClass.getAnnotation(ConfigurationProperties.class);
    Assert.notNull(annotation,
        "No " + ConfigurationProperties.class.getSimpleName() + " annotation found on  '"
            + configurationClass.getName() + "'.");
    return StringUtils.hasLength(annotation.value()) ? annotation.value() : annotation.prefix();
  }

  /**
   * create and bind configuration object with environment properties
   * <p>
   * Note: the configuration class must be annotated with
   * {@link ConfigurationProperties}
   * </p>
   *
   * @param configurationClass
   * @return Return new instance.
   * @see ConfigurationProperties
   * @since 2.2
   */
  @SuppressWarnings("unchecked")
  public <T> T createAndBindConfiguration(final Class<T> configurationClass) {
    Object bean = null;
    try {
      bean = configurationClass.getDeclaredConstructor().newInstance();
    } catch (final Exception e) {
      throw new RuntimeException(e.getMessage(), e);
    }
    bindConfiguration(bean);
    return (T) bean;
  }

  /**
   * Binds configuration properties object with environment properties
   * <p>
   * Note: the configuration properties class must be annotated with
   * {@link ConfigurationProperties}
   * </p>
   *
   * @return Return new instance.
   * @see ConfigurationProperties
   * @since 2.2
   */
  public void bindConfiguration(final Object properties) {
    final ConfigurationPropertiesBindingPostProcessor processor = getConfigurationPropertiesBindingPostProcessor();
    processor.postProcessBeforeInitialization(properties, properties.getClass().getName());
  }

  public List<PropertyPath> resolvePath(@Nonnull final Object properties) {
    Assert.checkNotNull(properties, "properties");
    final String targetName = getTargetName(properties.getClass());

    final BeanWrapperImpl beanWrapper = new BeanWrapperImpl(properties);

    final List<PropertyPath> propertyPaths = Lists.newLinkedList();

    resolvePath(propertyPaths, beanWrapper, targetName);

    return propertyPaths;
  }

  protected ConfigurationPropertiesBindingPostProcessor getConfigurationPropertiesBindingPostProcessor() {
    return beanFactory.getBean(ConfigurationPropertiesBindingPostProcessorRegistrar.BINDER_BEAN_NAME,
        ConfigurationPropertiesBindingPostProcessor.class);
  }

  private void resolvePath(final List<PropertyPath> propertyPaths,
      final BeanWrapper beanWrapper,
      final String targetName) {
    final PropertyDescriptor[] propertyDescriptors = beanWrapper.getPropertyDescriptors();
    for (final PropertyDescriptor propertyDescriptor : propertyDescriptors) {
      final String propertyName = propertyDescriptor.getName();
      // is primitive
      final Class<?> propertyType = propertyDescriptor.getPropertyType();
      if (BeanUtils.isSimpleProperty(propertyType) || propertyType.isInterface()) {
        if (beanWrapper.isWritableProperty(propertyName)
            && !isPresentAnnotationPropertyDescriptor(beanWrapper.getWrappedClass(),
                propertyDescriptor,
                NoPersistent.class)) {
          final PropertyPath path = new PropertyPath(beanWrapper, propertyDescriptor, targetName,
              propertyName);
          propertyPaths.add(path);
        }
      } else {
        Object value = beanWrapper.getPropertyValue(propertyName);
        if (value == null) {
          value = BeanUtils.instantiateClass(propertyType);
        }
        final String nestedPath = PropertyPath.joinString(targetName, propertyName);
        final BeanWrapperImpl bean = new BeanWrapperImpl(value, nestedPath, beanWrapper);
        resolvePath(propertyPaths, bean, nestedPath);
      }

    }

  }

  private boolean isPresentAnnotationPropertyDescriptor(final Class<?> parentClass,
      final PropertyDescriptor descriptor,
      final Class<? extends Annotation> annotationClass) {
    return findPropertyDescriptorAnnotation(parentClass, descriptor, annotationClass) != null;
  }

  @Nullable
  private <T extends Annotation> T findPropertyDescriptorAnnotation(final Class<?> parentClass,
      final PropertyDescriptor descriptor,
      final Class<T> annotationClass) {
    T annotation = null;
    annotation = descriptor.getWriteMethod().getAnnotation(annotationClass);
    if (annotation == null) {
      annotation = descriptor.getReadMethod().getAnnotation(annotationClass);
      if (annotation == null) {
        final Field field = ReflectionUtils.findField(parentClass, descriptor.getName());
        if (field != null) {
          annotation = field.getAnnotation(annotationClass);

        }
      }
    }
    return annotation;
  }

  /**
   * @author Christophe Friederich
   * @since 2.2
   */
  public static class PropertyPath {

    /** */
    private final BeanWrapper beanWrapper;

    /** */
    private final String path;

    /** */
    private final PropertyDescriptor propertyDescriptor;

    public PropertyPath(final BeanWrapper beanWrapper, final PropertyDescriptor propertyDescriptor,
        final String prefix, final String path) {
      this.beanWrapper = checkNotNull(beanWrapper, "beanWrapper");
      this.path = joinString(stripLastDot(prefix), path);
      this.propertyDescriptor = checkNotNull(propertyDescriptor, "propertyDescriptor");
    }

    public BeanWrapper getBeanWrapper() {
      return beanWrapper;
    }

    public String getPath() {
      return path;
    }

    public Class<?> getPropertyType() {
      return this.propertyDescriptor.getPropertyType();
    }

    void setValue(final Object value) {
      this.beanWrapper.setPropertyValue(this.propertyDescriptor.getName(), value);
    }

    public Object getPropertyValue() {
      return this.beanWrapper.getPropertyValue(this.propertyDescriptor.getName());

    }

    @Override
    public String toString() {
      return MoreObjects.toStringHelper(this).add("path", path).toString();
    }

    private static String stripLastDot(String string) {
      if (StringUtils.hasLength(string) && string.endsWith(PropertyAccessor.NESTED_PROPERTY_SEPARATOR)) {
        string = string.substring(0, string.length() - 1);
      }
      return string;
    }

    private static String joinString(final String prefix, final String name) {
      return StringUtils.hasLength(prefix) ? prefix + PropertyAccessor.NESTED_PROPERTY_SEPARATOR + name : name;
    }

  }

}
