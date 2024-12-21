package com.pmi.tpd.spring.env;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.Resource;

/**
 * Strategy to load '.properties' files into a {@link PropertySource}.
 *
 * @author Dave Syer
 * @author Christophe Friederich
 * @since 1.0
 */
public class PropertiesPropertySourceLoader implements PropertySourceLoader {

    private static final String XML_FILE_EXTENSION = ".xml";

    @Override
    public String[] getFileExtensions() {
        return new String[] { "properties", "xml" };
    }

    @Override
    public PropertySource<?> load(final String name, final Resource resource, final String profile) throws IOException {
        if (profile == null) {
            final Properties properties = loadProperties(resource);
            if (!properties.isEmpty()) {
                return new PropertiesPropertySource(name, properties);
            }
        }
        return null;
    }

    /**
     * Load properties from the given resource (in ISO-8859-1 encoding).
     *
     * @param resource
     *            the resource to load from
     * @return the populated Properties instance
     * @throws IOException
     *             if loading failed
     * @see #fillProperties(java.util.Properties, Resource)
     */
    public static Properties loadProperties(final Resource resource) throws IOException {
        final Properties props = new Properties();
        fillProperties(props, resource);
        return props;
    }

    /**
     * Fill the given properties from the given resource (in ISO-8859-1 encoding).
     *
     * @param props
     *            the Properties instance to fill
     * @param resource
     *            the resource to load from
     * @throws IOException
     *             if loading failed
     */
    public static void fillProperties(final Properties props, final Resource resource) throws IOException {
        final InputStream is = resource.getInputStream();
        if (is == null) {
            return;
        }
        try {
            final String filename = resource.getFilename();
            if (filename != null && filename.endsWith(XML_FILE_EXTENSION)) {
                props.loadFromXML(is);
            } else {
                props.load(is);
            }
        } finally {
            is.close();
        }
    }

}
