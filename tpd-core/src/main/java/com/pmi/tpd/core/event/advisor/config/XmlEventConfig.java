package com.pmi.tpd.core.event.advisor.config;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.UndeclaredThrowableException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import javax.annotation.Nonnull;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.SAXException;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.pmi.tpd.api.event.advisor.EventLevel;
import com.pmi.tpd.api.event.advisor.event.EventType;
import com.pmi.tpd.api.event.advisor.event.IApplicationEventCheck;
import com.pmi.tpd.api.event.advisor.event.IEventCheck;
import com.pmi.tpd.api.util.ClassLoaderUtils;
import com.pmi.tpd.core.event.advisor.IConfigurable;
import com.pmi.tpd.core.event.advisor.IContainerFactory;
import com.pmi.tpd.core.event.advisor.IRequestEventCheck;
import com.pmi.tpd.core.event.advisor.servlet.ServletContainerFactory;
import com.pmi.tpd.core.event.advisor.setup.DefaultSetupConfig;
import com.pmi.tpd.core.event.advisor.setup.ISetupConfig;
import com.pmi.tpd.core.util.path.DefaultPathMapper;
import com.pmi.tpd.core.util.path.PathMapper;

/**
 * Loads configuration from an XML file.
 *
 * @author Christophe Friederich
 * @since 1.3
 */
public final class XmlEventConfig implements IEventConfig {

    /** */
    public static final String DEFAULT_CONFIGURATION_FILE = "event-config.xml";

    /** */
    private static final Logger LOG = LoggerFactory.getLogger(XmlEventConfig.class);

    /** */
    private final List<IApplicationEventCheck<?>> applicationEventChecks;

    /** */
    private final IContainerFactory containerFactory;

    /** */
    private final String errorPath;

    /** */
    private final List<IEventCheck> eventChecks;

    /** */
    private final Map<Integer, IEventCheck> eventChecksById;

    /** */
    private final Map<String, EventLevel> eventLevels;

    /** */
    private final Map<String, EventType> eventTypes;

    /** */
    private final PathMapper ignoreMapper;

    /** */
    private final List<String> ignorePaths;

    /** */
    private final Map<String, String> params;

    /** */
    private final List<IRequestEventCheck<?>> requestEventChecks;

    /** */
    private final ISetupConfig setupConfig;

    /** */
    private final String setupPath;

    private XmlEventConfig(final ISetupConfig setupConfig, final IContainerFactory containerFactory,
            final List<IEventCheck> eventChecks, final Map<Integer, IEventCheck> eventChecksById,
            final Map<String, EventLevel> eventLevels, final Map<String, EventType> eventTypes,
            final List<String> ignorePaths, final Map<String, String> params, final String setupPath,
            final String errorPath) {
        this.containerFactory = containerFactory;
        this.errorPath = errorPath;
        this.eventChecks = eventChecks;
        this.eventChecksById = eventChecksById;
        this.eventLevels = eventLevels;
        this.eventTypes = eventTypes;
        this.ignorePaths = ignorePaths;
        this.params = params;
        this.setupConfig = setupConfig;
        this.setupPath = setupPath;

        final ImmutableList.Builder<IApplicationEventCheck<?>> applicationBuilder = ImmutableList.builder();
        final ImmutableList.Builder<IRequestEventCheck<?>> requestBuilder = ImmutableList.builder();
        for (final IEventCheck eventCheck : eventChecks) {
            if (eventCheck instanceof IApplicationEventCheck) {
                applicationBuilder.add((IApplicationEventCheck<?>) eventCheck);
            }
            if (eventCheck instanceof IRequestEventCheck) {
                requestBuilder.add((IRequestEventCheck<?>) eventCheck);
            }
        }
        applicationEventChecks = applicationBuilder.build();
        requestEventChecks = requestBuilder.build();

        ignoreMapper = new DefaultPathMapper();
        ignoreMapper.put(errorPath, errorPath);
        ignoreMapper.put(setupPath, setupPath);
        for (final String path : ignorePaths) {
            ignoreMapper.put(path, path);
        }
    }

    /**
     * @param document
     * @return
     */
    @Nonnull
    public static XmlEventConfig fromDocument(@Nonnull final Document document) {
        final Element root = checkNotNull(document, "document").getDocumentElement();

        final ISetupConfig setupConfig = configureClass(root,
            "setup-config",
            ISetupConfig.class,
            DefaultSetupConfig.class);
        final IContainerFactory containerFactory = configureClass(root,
            "container-factory",
            IContainerFactory.class,
            ServletContainerFactory.class);
        final Map<String, EventLevel> eventLevels = configureEventConstants(root, "event-levels", EventLevel.class);
        final Map<String, EventType> eventTypes = configureEventConstants(root, "event-types", EventType.class);
        final Map<String, String> params = configureParameters(root);
        final String setupPath = Iterables.getOnlyElement(configurePaths(root, "setup"));
        final String errorPath = Iterables.getOnlyElement(configurePaths(root, "error"));
        final List<String> ignorePaths = configurePaths(root, "ignore");

        ElementIterable elements = getElementsByTagName(root, "event-checks");

        final ArrayList<IEventCheck> checks = new ArrayList<>(elements.size());
        final Map<Integer, IEventCheck> checksById = new HashMap<>(elements.size());
        if (!elements.isEmpty()) {
            elements = getElementsByTagName(Iterables.getOnlyElement(elements), "event-check");
            for (final Element element : elements) {
                final IEventCheck check = parseEventCheck(element);
                checks.add(check);

                final String id = element.getAttribute("id");
                if (StringUtils.isNotBlank(id)) {
                    try {
                        if (checksById.put(Integer.parseInt(id), check) != null) {
                            throw new ConfigurationEventException("EventCheck ID [" + id + "] is not unique");
                        }
                    } catch (final NumberFormatException e) {
                        throw new ConfigurationEventException("EventCheck ID [" + id + "] is not a number", e);
                    }
                }
            }
        }

        return new XmlEventConfig(setupConfig, containerFactory, ImmutableList.copyOf(checks),
                ImmutableMap.copyOf(checksById), eventLevels, eventTypes, ignorePaths, params, setupPath, errorPath);
    }

    /**
     * @param fileName
     * @return
     */
    @Nonnull
    public static XmlEventConfig fromFile(@Nonnull String fileName) {
        final URL url = ClassLoaderUtils.getResource(checkNotNull(fileName, "fileName"), XmlEventConfig.class);
        if (url != null) {
            LOG.debug("Loading {} from classpath at {}", fileName, url);
            fileName = url.toString();
        }

        final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        try {
            final DocumentBuilder builder = factory.newDocumentBuilder();
            final Document document = builder.parse(fileName);

            return fromDocument(document);
        } catch (final IOException e) {
            throw new ConfigurationEventException("Failed to parse [" + fileName + "]; the file could not be read", e);
        } catch (final ParserConfigurationException e) {
            throw new ConfigurationEventException("Failed to parse [" + fileName + "]; JVM configuration is invalid",
                    e);
        } catch (final SAXException e) {
            throw new ConfigurationEventException("Failed to parse [" + fileName + "]; XML is not well-formed", e);
        }
    }

    @Nonnull
    public static XmlEventConfig fromInputStream(@Nonnull final InputStream input) {
        checkNotNull(input, "input");

        final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        try {
            final DocumentBuilder builder = factory.newDocumentBuilder();
            final Document document = builder.parse(input);

            return fromDocument(document);
        } catch (final IOException e) {
            throw new ConfigurationEventException("Failed to parse the file could not be read", e);
        } catch (final ParserConfigurationException e) {
            throw new ConfigurationEventException("Failed to parse JVM configuration is invalid", e);
        } catch (final SAXException e) {
            throw new ConfigurationEventException("Failed to parse XML is not well-formed", e);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    @Nonnull
    public <CONTEXT> List<IApplicationEventCheck<CONTEXT>> getApplicationEventChecks() {
        final List<IApplicationEventCheck<CONTEXT>> l = Lists.newArrayListWithCapacity(applicationEventChecks.size());
        for (final IApplicationEventCheck<?> eventCheck : applicationEventChecks) {
            l.add((IApplicationEventCheck<CONTEXT>) eventCheck);
        }
        return l;
    }

    @Override
    @Nonnull
    public IContainerFactory getContainerFactory() {
        return containerFactory;
    }

    @Override
    @Nonnull
    public String getErrorPath() {
        return errorPath;
    }

    @Override
    public IEventCheck getEventCheck(final int id) {
        return eventChecksById.get(id);
    }

    @Override
    @Nonnull
    public List<IEventCheck> getEventChecks() {
        return eventChecks;
    }

    @Override
    public EventLevel getEventLevel(@Nonnull final String level) {
        return eventLevels.get(checkNotNull(level, "level"));
    }

    @Override
    public EventType getEventType(@Nonnull final String type) {
        return eventTypes.get(checkNotNull(type, "type"));
    }

    @Override
    @Nonnull
    public List<String> getIgnorePaths() {
        return ignorePaths;
    }

    @Override
    @Nonnull
    public Map<String, String> getParams() {
        return params;
    }

    @SuppressWarnings("unchecked")
    @Override
    @Nonnull
    public <REQUEST> List<IRequestEventCheck<REQUEST>> getRequestEventChecks() {
        final List<IRequestEventCheck<REQUEST>> l = Lists.newArrayListWithCapacity(requestEventChecks.size());
        for (final IRequestEventCheck<?> eventCheck : requestEventChecks) {
            l.add((IRequestEventCheck<REQUEST>) eventCheck);
        }
        return l;
    }

    @Override
    @Nonnull
    public ISetupConfig getSetupConfig() {
        return setupConfig;
    }

    @Override
    @Nonnull
    public String getSetupPath() {
        return setupPath;
    }

    @Override
    public boolean isIgnoredPath(@Nonnull final String uri) {
        return ignoreMapper.get(checkNotNull(uri, "uri")) != null;
    }

    private static <T> Map<String, T> configureEventConstants(final Element root,
        final String tagName,
        final Class<T> childClass) {
        Constructor<T> constructor;
        try {
            constructor = childClass.getConstructor(String.class, String.class);
        } catch (final NoSuchMethodException e) {
            throw new IllegalArgumentException(
                    "Class [" + childClass.getName() + "] requires a String, String constructor");
        }

        ElementIterable elements = getElementsByTagName(root, tagName);
        if (elements.isEmpty()) {
            return Collections.emptyMap();
        }
        elements = getElementsByTagName(Iterables.getOnlyElement(elements), tagName.substring(0, tagName.length() - 1));

        final ImmutableMap.Builder<String, T> builder = ImmutableMap.builder();
        for (final Element element : elements) {
            final String key = element.getAttribute("key");
            final String description = getContainedText(element, "description");

            try {
                builder.put(key, constructor.newInstance(key, description));
            } catch (final IllegalAccessException e) {
                throw new IllegalArgumentException("Constructor [" + constructor.getName() + "] must be public");
            } catch (final InstantiationException e) {
                throw new IllegalArgumentException("Class [" + childClass.getName() + "] may not be abstract");
            } catch (final InvocationTargetException e) {
                final Throwable cause = e.getCause();
                if (cause instanceof RuntimeException) {
                    throw (RuntimeException) cause;
                }
                throw new UndeclaredThrowableException(cause);
            }
        }
        return builder.build();
    }

    private static List<String> configurePaths(final Element root, final String tagname) {
        ElementIterable elements = getElementsByTagName(root, tagname);
        if (elements.isEmpty()) {
            return Collections.emptyList();
        }
        elements = getElementsByTagName(Iterables.getOnlyElement(elements), "path");

        return ImmutableList
                .copyOf(Iterables.transform(elements, input -> ((Text) input.getFirstChild()).getData().trim()));
    }

    private static Map<String, String> configureParameters(final Element root) {
        final NodeList list = root.getElementsByTagName("parameters");
        if (isEmpty(list)) {
            return Collections.emptyMap();
        }

        final Element element = (Element) list.item(0);
        return getInitParameters(element);
    }

    @Nonnull
    private static <T> T configureClass(final Element root,
        final String tagname,
        final Class<T> expectedClass,
        final Class<? extends T> defaultClass) {
        final ElementIterable elements = getElementsByTagName(root, tagname);
        if (elements.isEmpty()) {
            try {
                return defaultClass.getConstructor().newInstance();
            } catch (final Exception e) {
                throw new ConfigurationEventException(
                        "Default [" + expectedClass.getName() + "], [" + defaultClass.getName() + "] is not valid", e);
            }
        }

        final Element element = Iterables.getOnlyElement(elements);
        final String className = element.getAttribute("class");
        try {
            final Class<?> clazz = ClassLoaderUtils.loadClass(className, XmlEventConfig.class);
            if (!expectedClass.isAssignableFrom(clazz)) {
                throw new ConfigurationEventException("The class specified by " + tagname + " (" + className
                        + ") is required to implement [" + expectedClass.getName() + "]");
            }

            final T instance = expectedClass.cast(clazz.getConstructor().newInstance());
            if (instance instanceof IConfigurable) {
                final Map<String, String> params = getInitParameters(element);
                ((IConfigurable) instance).init(params);
            }
            return instance;
        } catch (final Exception e) {
            throw new ConfigurationEventException("Could not create: " + tagname, e);
        }
    }

    private static Map<String, String> getInitParameters(final Element root) {
        final ElementIterable elements = new ElementIterable(root.getElementsByTagName("init-param"));

        final ImmutableMap.Builder<String, String> builder = ImmutableMap.builder();
        for (final Element element : elements) {
            final String paramName = getContainedText(element, "param-name");
            final String paramValue = getContainedText(element, "param-value");
            builder.put(paramName, paramValue);
        }
        return builder.build();
    }

    private static String getContainedText(final Node parent, final String childTagName) {
        try {
            final Node tag = ((Element) parent).getElementsByTagName(childTagName).item(0);
            return ((Text) tag.getFirstChild()).getData();
        } catch (final Exception e) {
            return null;
        }
    }

    private static ElementIterable getElementsByTagName(final Node parent, final String tagName) {
        final Element element = (Element) parent;
        NodeList list = element.getElementsByTagName(tagName);
        if (isEmpty(list) && tagName.contains("-")) {
            // Many tags used to not have hyphens, so we fall back to the old run-together approach
            list = element.getElementsByTagName(tagName.replace("-", ""));
        }

        return new ElementIterable(list);
    }

    private static boolean isEmpty(final NodeList list) {
        return list == null || list.getLength() == 0;
    }

    private static IEventCheck parseEventCheck(final Element element) {
        final String className = element.getAttribute("class");
        if (StringUtils.isBlank(className)) {
            throw new ConfigurationEventException("event-check element with bad class attribute");
        }

        Object o;
        try {
            LOG.trace("Loading class [{}]", className);
            final Class<?> eventCheckClazz = ClassLoaderUtils.loadClass(className, XmlEventConfig.class);
            LOG.trace("Instantiating [{}]", className);
            o = eventCheckClazz.getConstructor().newInstance();
        } catch (final ClassNotFoundException e) {
            LOG.error("Failed to load EventCheck class [" + className + "]", e);
            throw new ConfigurationEventException("Could not load EventCheck: " + className, e);
        } catch (final IllegalAccessException e) {
            LOG.error("Missing public nullary constructor for EventCheck class [" + className + "]", e);
            throw new ConfigurationEventException("Could not instantiate EventCheck: " + className, e);
        } catch (final InstantiationException | InvocationTargetException | NoSuchMethodException e) {
            LOG.error("Could not instantiate EventCheck class [" + className + "]", e);
            throw new ConfigurationEventException("Could not instantiate EventCheck: " + className, e);
        }

        if (!(o instanceof IEventCheck)) {
            throw new ConfigurationEventException(className + " does not implement EventCheck");
        }

        LOG.debug("Adding EventCheck of class: " + className);
        final IEventCheck eventCheck = (IEventCheck) o;
        if (eventCheck instanceof IConfigurable) {
            ((IConfigurable) eventCheck).init(getInitParameters(element));
        }
        return eventCheck;
    }

    /**
     * @author Christophe Friederich
     * @since 1.3
     */
    private static final class ElementIterable implements Iterable<Element> {

        /** */
        private final NodeList list;

        private ElementIterable(final NodeList list) {
            this.list = list;
        }

        @Override
        public Iterator<Element> iterator() {
            return new Iterator<>() {

                private int index;

                @Override
                public boolean hasNext() {
                    return index < list.getLength();
                }

                @Override
                public Element next() {
                    if (hasNext()) {
                        return (Element) list.item(index++);
                    }
                    throw new NoSuchElementException();
                }

                @Override
                public void remove() {
                    throw new UnsupportedOperationException();
                }
            };
        }

        public boolean isEmpty() {
            return list == null || list.getLength() == 0;
        }

        public int size() {
            return list == null ? 0 : list.getLength();
        }
    }
}
