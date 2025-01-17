package com.pmi.tpd.spring.env;

import java.io.IOException;
import java.io.InputStream;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.nodes.MappingNode;
import org.yaml.snakeyaml.parser.ParserException;

/**
 * Base class for Yaml factories.
 *
 * @author Dave Syer
 * @author Christophe Friederich
 * @since 1.0
 */
public abstract class YamlProcessor {

    /** logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(YamlProcessor.class);

    /** the resolution method. */
    private ResolutionMethod resolutionMethod = ResolutionMethod.OVERRIDE;

    /** list of resources to load. */
    private Resource[] resources = new Resource[0];

    /** list of document matchers. */
    private List<DocumentMatcher> documentMatchers = Collections.emptyList();

    /** execute callback although no match has been found. */
    private boolean matchDefault = true;

    /**
     * A map of document matchers allowing callers to selectively use only some of the documents in a YAML resource. In
     * YAML documents are separated by <code>---</code> lines, and each document is converted to properties before the
     * match is made. E.g.
     *
     * <pre class="code">
     * environment: dev
     * url: http://dev.bar.com
     * name: Developer Setup
     * ---
     * environment: prod
     * url:http://foo.bar.com
     * name: My Cool App
     * </pre>
     *
     * when mapped with <code>documentMatchers = YamlProcessor.mapMatcher({"environment": "prod"})</code> would end up
     * as
     *
     * <pre class="code">
     * environment=prod
     * url=http://foo.bar.com
     * name=My Cool App
     * url=http://dev.bar.com
     * </pre>
     *
     * @param matchers
     *            a map of keys to value patterns (regular expressions)
     */
    public void setDocumentMatchers(final DocumentMatcher... matchers) {
        this.documentMatchers = Arrays.asList(matchers);
    }

    /**
     * Flag indicating that a document for which all the {@link #setDocumentMatchers(DocumentMatcher...) document
     * matchers} abstain will nevertheless match.
     *
     * @param matchDefault
     *            the flag to set (default true)
     */
    public void setMatchDefault(final boolean matchDefault) {
        this.matchDefault = matchDefault;
    }

    /**
     * Method to use for resolving resources. Each resource will be converted to a Map, so this property is used to
     * decide which map entries to keep in the final output from this factory.
     *
     * @param resolutionMethod
     *            the resolution method to set (defaults to {@link ResolutionMethod#OVERRIDE}).
     */
    public void setResolutionMethod(final ResolutionMethod resolutionMethod) {
        Assert.notNull(resolutionMethod, "ResolutionMethod must not be null");
        this.resolutionMethod = resolutionMethod;
    }

    /**
     * Set locations of YAML {@link Resource resources} to be loaded.
     *
     * @param resources
     *            list of resources to loaded.
     * @see ResolutionMethod
     */
    public void setResources(final Resource... resources) {
        this.resources = resources;
    }

    /**
     * Provide an opportunity for subclasses to process the Yaml parsed from the supplied resources. Each resource is
     * parsed in turn and the documents inside checked against the {@link #setDocumentMatchers(DocumentMatcher...)
     * matchers}. If a document matches it is passed into the callback, along with its representation as Properties.
     * Depending on the {@link #setResolutionMethod(ResolutionMethod)} not all of the documents will be parsed.
     *
     * @param callback
     *            a callback to delegate to once matching documents are found
     * @see #createYaml()
     */
    protected void process(final MatchCallback callback) {
        final Yaml yaml = createYaml();
        for (final Resource resource : this.resources) {
            final boolean found = process(callback, yaml, resource);
            if (this.resolutionMethod == ResolutionMethod.FIRST_FOUND && found) {
                return;
            }
        }
    }

    /**
     * Create the {@link Yaml} instance to use.
     *
     * @return Returns new instance of {@link Yaml}.
     */
    protected Yaml createYaml() {
        return new Yaml(new StrictMapAppenderConstructor());
    }

    private boolean process(final MatchCallback callback, final Yaml yaml, final Resource resource) {
        int count = 0;
        try {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Loading from YAML: " + resource);
            }
            final InputStream stream = resource.getInputStream();
            try {
                for (final Object object : yaml.loadAll(stream)) {
                    if (object != null && process(asMap(object), callback)) {
                        count++;
                        if (this.resolutionMethod == ResolutionMethod.FIRST_FOUND) {
                            break;
                        }
                    }
                }
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug(
                        "Loaded " + count + " document" + (count > 1 ? "s" : "") + " from YAML resource: " + resource);
                }
            } finally {
                stream.close();
            }
        } catch (final IOException ex) {
            handleProcessError(resource, ex);
        }
        return count > 0;
    }

    private void handleProcessError(final Resource resource, final IOException ex) {
        if (this.resolutionMethod != ResolutionMethod.FIRST_FOUND
                && this.resolutionMethod != ResolutionMethod.OVERRIDE_AND_IGNORE) {
            throw new IllegalStateException(ex);
        }
        if (LOGGER.isWarnEnabled()) {
            LOGGER.warn("Could not load map from " + resource + ": " + ex.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> asMap(final Object object) {
        // YAML can have numbers as keys
        final Map<String, Object> result = new LinkedHashMap<>();
        if (!(object instanceof Map)) {
            // A document can be a text literal
            result.put("document", object);
            return result;
        }

        final Map<Object, Object> map = (Map<Object, Object>) object;
        for (final Entry<Object, Object> entry : map.entrySet()) {
            Object value = entry.getValue();
            if (value instanceof Map) {
                value = asMap(value);
            }
            final Object key = entry.getKey();
            if (key instanceof CharSequence) {
                result.put(key.toString(), value);
            } else {
                // It has to be a map key in this case
                result.put("[" + key.toString() + "]", value);
            }
        }
        return result;
    }

    private boolean process(final Map<String, Object> map, final MatchCallback callback) {
        final Properties properties = new Properties();
        properties.putAll(getFlattenedMap(map));

        if (this.documentMatchers.isEmpty()) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Merging document (no matchers set)" + map);
            }
            callback.process(properties, map);
            return true;
        }

        MatchStatus result = MatchStatus.ABSTAIN;
        for (final DocumentMatcher matcher : this.documentMatchers) {
            final MatchStatus match = matcher.matches(properties);
            result = MatchStatus.getMostSpecific(match, result);
            if (match == MatchStatus.FOUND) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Matched document with document matcher: " + properties);
                }
                callback.process(properties, map);
                return true;
            }
        }

        if (result == MatchStatus.ABSTAIN && this.matchDefault) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Matched document with default matcher: " + map);
            }
            callback.process(properties, map);
            return true;
        }

        LOGGER.debug("Unmatched document");
        return false;
    }

    /**
     * Return a flattened version of the given map, recursively following any nested Map or Collection values. Entries
     * from the resulting map retain the same order as the source. When called with the Map from a {@link MatchCallback}
     * the result will contain the same values as the {@link MatchCallback} Properties.
     *
     * @param source
     *            the source map
     * @return a flattened map
     */
    protected final Map<String, Object> getFlattenedMap(final Map<String, Object> source) {
        final Map<String, Object> result = new LinkedHashMap<>();
        buildFlattenedMap(result, source, null);
        return result;
    }

    private void buildFlattenedMap(final Map<String, Object> result,
        final Map<String, Object> source,
        final String path) {
        for (final Entry<String, Object> entry : source.entrySet()) {
            String key = entry.getKey();
            if (StringUtils.hasText(path)) {
                if (key.startsWith("[")) {
                    key = path + key;
                } else {
                    key = path + "." + key;
                }
            }
            final Object value = entry.getValue();
            if (value instanceof String) {
                result.put(key, value);
            } else if (value instanceof Map) {
                // Need a compound key
                @SuppressWarnings("unchecked")
                final Map<String, Object> map = (Map<String, Object>) value;
                buildFlattenedMap(result, map, key);
            } else if (value instanceof Collection) {
                // Need a compound key
                @SuppressWarnings("unchecked")
                final Collection<Object> collection = (Collection<Object>) value;
                int count = 0;
                for (final Object object : collection) {
                    buildFlattenedMap(result, Collections.singletonMap("[" + count++ + "]", object), key);
                }
            } else {
                result.put(key, value == null ? "" : value);
            }
        }
    }

    /**
     * Callback interface used to process properties in a resulting map.
     */
    public interface MatchCallback {

        /**
         * Process the properties.
         *
         * @param properties
         *            the properties to process
         * @param map
         *            a mutable result map
         */
        void process(Properties properties, Map<String, Object> map);
    }

    /**
     * Strategy interface used to test if properties match.
     */
    public interface DocumentMatcher {

        /**
         * Test if the given properties match.
         *
         * @param properties
         *            the properties to test
         * @return the status of the match.
         */
        MatchStatus matches(Properties properties);
    }

    /**
     * Status returned from {@link DocumentMatcher#matches(java.util.Properties)}.
     */
    public enum MatchStatus {

        /**
         * A match was found.
         */
        FOUND,

        /**
         * No match was found.
         */
        NOT_FOUND,

        /**
         * The matcher should not be considered.
         */
        ABSTAIN;

        /**
         * Gets the most specific status.
         *
         * @param a
         *            a match status.
         * @param b
         *            another match status
         * @return Returns the most specific status.
         */
        public static MatchStatus getMostSpecific(final MatchStatus a, final MatchStatus b) {
            return a.ordinal() < b.ordinal() ? a : b;
        }
    }

    /**
     * Method to use for resolving resources.
     */
    public enum ResolutionMethod {

        /**
         * Replace values from earlier in the list.
         */
        OVERRIDE,

        /**
         * Replace values from earlier in the list, ignoring any failures.
         */
        OVERRIDE_AND_IGNORE,

        /**
         * Take the first resource in the list that exists and use just that.
         */
        FIRST_FOUND
    }

    /**
     * A specialized {@link Constructor} that checks for duplicate keys.
     */
    protected static class StrictMapAppenderConstructor extends Constructor {

        /**
         * Default constructor.
         */
        public StrictMapAppenderConstructor() {
            super();
        }

        @SuppressWarnings("PMD.PreserveStackTrace")
        @Override
        protected Map<Object, Object> constructMapping(final MappingNode node) {
            try {
                return super.constructMapping(node);
            } catch (final IllegalStateException e) {
                throw new ParserException("while parsing MappingNode", node.getStartMark(), e.getMessage(),
                        node.getEndMark());
            }
        }

        @Override
        protected Map<Object, Object> createDefaultMap(final int initSize) {
            final Map<Object, Object> delegate = super.createDefaultMap(initSize);
            return new AbstractMap<>() {

                @Override
                public Object put(final Object key, final Object value) {
                    if (delegate.containsKey(key)) {
                        throw new IllegalStateException("duplicate key: " + key);
                    }
                    return delegate.put(key, value);
                }

                @Override
                public Set<Entry<Object, Object>> entrySet() {
                    return delegate.entrySet();
                }
            };
        }

    }

}
