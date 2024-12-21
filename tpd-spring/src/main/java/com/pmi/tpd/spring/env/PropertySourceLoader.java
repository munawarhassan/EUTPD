package com.pmi.tpd.spring.env;

import java.io.IOException;

import org.springframework.core.env.PropertySource;
import org.springframework.core.io.Resource;

/**
 * Strategy interface located via {@link org.springframework.core.io.support.SpringFactoriesLoader
 * SpringFactoriesLoader} and used to load a {@link PropertySource}.
 *
 * @author Dave Syer
 * @author Christophe Friederich
 * @since 1.0
 */
public interface PropertySourceLoader {

    /**
     * Returns the file extensions that the loader supports (excluding the '.').
     *
     * @return the file extensions
     */
    String[] getFileExtensions();

    /**
     * Load the resource into a property source.
     *
     * @param name
     *            the name of the property source
     * @param resource
     *            the resource to load
     * @param profile
     *            the name of the profile to load or {@code null}. The profile can be used to load multi-document files
     *            (such as YAML). Simple property formats should {@code null} when asked to load a profile.
     * @return a property source or {@code null}
     * @throws IOException
     *             if loading failed.
     */
    PropertySource<?> load(String name, Resource resource, String profile) throws IOException;

}
