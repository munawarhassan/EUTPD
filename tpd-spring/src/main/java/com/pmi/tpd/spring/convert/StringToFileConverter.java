package com.pmi.tpd.spring.convert;

import java.io.File;
import java.io.IOException;

import org.springframework.core.convert.converter.Converter;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.ResourceUtils;

/**
 * {@link Converter} to convert from a {@link String} to a {@link File}. Supports basic file conversion as well as file
 * URLs.
 *
 * @author Phillip Webb
 */
class StringToFileConverter implements Converter<String, File> {

    private static final ResourceLoader resourceLoader = new DefaultResourceLoader(null);

    @Override
    public File convert(final String source) {
        if (ResourceUtils.isUrl(source)) {
            return getFile(resourceLoader.getResource(source));
        }
        final File file = new File(source);
        if (file.exists()) {
            return file;
        }
        final Resource resource = resourceLoader.getResource(source);
        if (resource.exists()) {
            return getFile(resource);
        }
        return file;
    }

    private File getFile(final Resource resource) {
        try {
            return resource.getFile();
        } catch (final IOException ex) {
            throw new IllegalStateException("Could not retrieve file for " + resource + ": " + ex.getMessage());
        }
    }

}