package com.pmi.tpd.spring.env;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.context.annotation.ImportSelector;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.Assert;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;

import com.pmi.tpd.api.config.annotation.ConfigurationProperties;

/**
 * Import selector that sets up binding of external properties to configuration classes (see
 * {@link ConfigurationProperties}). It either registers a {@link ConfigurationProperties} bean or not, depending on
 * whether the enclosing {@link EnableConfigurationProperties} explicitly declares one. If none is declared then a bean
 * post processor will still kick in for any beans annotated as external configuration. If one is declared then it a
 * bean definition is registered with id equal to the class name (thus an application context usually only contains one
 * {@link ConfigurationProperties} bean of each unique type).
 *
 * @author Dave Syer
 * @author Christian Dupuis
 * @author Stephane Nicoll
 */
class EnableConfigurationPropertiesImportSelector implements ImportSelector {

    @Override
    public String[] selectImports(final AnnotationMetadata metadata) {
        final MultiValueMap<String, Object> attributes = metadata
                .getAllAnnotationAttributes(EnableConfigurationProperties.class.getName(), false);
        final Object[] type = attributes == null ? null : (Object[]) attributes.getFirst("value");
        if (type == null || type.length == 0) {
            return new String[] { ConfigurationPropertiesBindingPostProcessorRegistrar.class.getName() };
        }
        return new String[] { ConfigurationPropertiesBeanRegistrar.class.getName(),
                ConfigurationPropertiesBindingPostProcessorRegistrar.class.getName() };
    }

    /**
     * {@link ImportBeanDefinitionRegistrar} for configuration properties support.
     */
    public static class ConfigurationPropertiesBeanRegistrar implements ImportBeanDefinitionRegistrar {

        @Override
        public void registerBeanDefinitions(final AnnotationMetadata metadata, final BeanDefinitionRegistry registry) {
            final MultiValueMap<String, Object> attributes = metadata
                    .getAllAnnotationAttributes(EnableConfigurationProperties.class.getName(), false);
            if (attributes == null) {
                return;
            }
            final List<Class<?>> types = collectClasses(attributes.get("value"));
            for (final Class<?> type : types) {
                final String prefix = extractPrefix(type);
                final String name = StringUtils.hasText(prefix) ? prefix + "-" + type.getName() : type.getName();
                if (!registry.containsBeanDefinition(name)) {
                    registerBeanDefinition(registry, type, name);
                }
            }
        }

        private String extractPrefix(final Class<?> type) {
            final ConfigurationProperties annotation = AnnotationUtils.findAnnotation(type,
                ConfigurationProperties.class);
            if (annotation != null) {
                return StringUtils.hasLength(annotation.value()) ? annotation.value() : annotation.prefix();
            }
            return "";
        }

        private List<Class<?>> collectClasses(final List<Object> list) {
            final ArrayList<Class<?>> result = new ArrayList<>();
            for (final Object object : list) {
                for (final Object value : (Object[]) object) {
                    if (value instanceof Class && value != void.class) {
                        result.add((Class<?>) value);
                    }
                }
            }
            return result;
        }

        private void registerBeanDefinition(final BeanDefinitionRegistry registry,
            final Class<?> type,
            final String name) {
            final BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(type);
            final AbstractBeanDefinition beanDefinition = builder.getBeanDefinition();
            registry.registerBeanDefinition(name, beanDefinition);

            final ConfigurationProperties properties = AnnotationUtils.findAnnotation(type,
                ConfigurationProperties.class);
            Assert.notNull(properties,
                "No " + ConfigurationProperties.class.getSimpleName() + " annotation found on  '" + type.getName()
                        + "'.");
        }

    }

}
