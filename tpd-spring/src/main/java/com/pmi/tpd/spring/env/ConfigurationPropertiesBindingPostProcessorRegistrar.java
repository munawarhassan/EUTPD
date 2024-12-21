package com.pmi.tpd.spring.env;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;

import com.pmi.tpd.api.config.annotation.ConfigurationProperties;

/**
 * {@link ImportBeanDefinitionRegistrar} for binding externalized application properties to
 * {@link ConfigurationProperties} beans.
 *
 * @author Dave Syer
 * @author Phillip Webb
 */
public class ConfigurationPropertiesBindingPostProcessorRegistrar implements ImportBeanDefinitionRegistrar {

    /**
     * The bean name of the {@link ConfigurationPropertiesBindingPostProcessor}.
     */
    public static final String BINDER_BEAN_NAME = ConfigurationPropertiesBindingPostProcessor.class.getName();

    private static final String METADATA_BEAN_NAME = BINDER_BEAN_NAME + ".store";

    @Override
    public void registerBeanDefinitions(final AnnotationMetadata importingClassMetadata,
        final BeanDefinitionRegistry registry) {
        if (!registry.containsBeanDefinition(BINDER_BEAN_NAME)) {
            final BeanDefinitionBuilder meta = BeanDefinitionBuilder
                    .genericBeanDefinition(ConfigurationBeanFactoryMetaData.class);
            final BeanDefinitionBuilder bean = BeanDefinitionBuilder
                    .genericBeanDefinition(ConfigurationPropertiesBindingPostProcessor.class);
            bean.addPropertyReference("beanMetaDataStore", METADATA_BEAN_NAME);
            registry.registerBeanDefinition(BINDER_BEAN_NAME, bean.getBeanDefinition());
            registry.registerBeanDefinition(METADATA_BEAN_NAME, meta.getBeanDefinition());
        }
    }

}
