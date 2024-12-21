package com.pmi.tpd.spring.env;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;

import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.Resource;
import org.springframework.util.ClassUtils;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.representer.Representer;
import org.yaml.snakeyaml.resolver.Resolver;

import com.pmi.tpd.spring.env.yaml.SpringProfileDocumentMatcher;

/**
 * Strategy to load '.yml' (or '.yaml') files into a {@link PropertySource}.
 *
 * @author Dave Syer
 * @author Christophe Friederich
 * @since 1.0
 */
public class YamlPropertySourceLoader implements PropertySourceLoader {

    @Override
    public String[] getFileExtensions() {
        return new String[] { "yml", "yaml" };
    }

    @Override
    public PropertySource<?> load(final String name, final Resource resource, final String profile) throws IOException {
        if (ClassUtils.isPresent("org.yaml.snakeyaml.Yaml", null)) {
            final Processor processor = new Processor(resource, profile);
            final Map<String, Object> source = processor.process();
            if (!source.isEmpty()) {
                return new MapPropertySource(name, source);
            }
        }
        return null;
    }

    /**
     * {@link YamlProcessor} to create a {@link Map} containing the property values. Similar to
     * {@link org.springframework.beans.factory.config.YamlPropertiesFactoryBean YamlPropertiesFactoryBean} but retains
     * the order of entries.
     */
    private static final class Processor extends YamlProcessor {

        /**
         * Create new instance of {@link Process}.
         *
         * @param resource
         *            yaml resource.
         * @param profile
         *            the name of the profile to process.
         */
        private Processor(final Resource resource, final String profile) {
            if (profile == null) {
                setMatchDefault(true);
                setDocumentMatchers(new SpringProfileDocumentMatcher());
            } else {
                setMatchDefault(false);
                setDocumentMatchers(new SpringProfileDocumentMatcher(profile));
            }
            setResources(new Resource[] { resource });
        }

        @Override
        protected Yaml createYaml() {
            return new Yaml(new StrictMapAppenderConstructor(), new Representer(), new DumperOptions(), new Resolver() {

                @Override
                public void addImplicitResolver(final Tag tag, final Pattern regexp, final String first) {
                    if (tag == Tag.TIMESTAMP) {
                        return;
                    }
                    super.addImplicitResolver(tag, regexp, first);
                }
            });
        }

        public Map<String, Object> process() {
            final Map<String, Object> result = new LinkedHashMap<>();
            process((properties, map) -> result.putAll(getFlattenedMap(map)));
            return result;
        }

    }

}
