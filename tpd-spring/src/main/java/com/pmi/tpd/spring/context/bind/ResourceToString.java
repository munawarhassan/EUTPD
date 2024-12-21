package com.pmi.tpd.spring.context.bind;

import org.springframework.core.convert.converter.Converter;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.web.context.support.ServletContextResource;

import com.pmi.tpd.spring.env.ConfigurationPropertiesBinding;

/**
 * @author Christophe Friederich
 */
@ConfigurationPropertiesBinding
public class ResourceToString implements Converter<Resource, String> {

    @Override
    public String convert(final Resource source) {
        String resourcePath = null;
        try {
            if (source instanceof UrlResource) {
                resourcePath = source.getURL().toExternalForm();
            } else if (source instanceof ClassPathResource) {
                resourcePath = ((ClassPathResource) source).getPath();
            } else if (source instanceof ServletContextResource) {
                resourcePath = ((ServletContextResource) source).getPath();
            } else {
                resourcePath = source.getURL().getPath();
            }
        } catch (final Exception ex) {
            throw new RuntimeException(ex.getMessage(), ex);
        }
        return resourcePath;
    }

}