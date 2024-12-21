package com.pmi.tpd.core.event.advisor.spring.web;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanInstantiationException;
import org.springframework.beans.BeanUtils;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

import com.pmi.tpd.api.event.advisor.EventLevel;
import com.pmi.tpd.api.event.advisor.event.Event;
import com.pmi.tpd.api.event.advisor.event.EventType;
import com.pmi.tpd.core.event.advisor.IConfigurable;
import com.pmi.tpd.core.event.advisor.config.IEventConfig;
import com.pmi.tpd.core.event.advisor.servlet.ServletEventAdvisor;
import com.pmi.tpd.core.event.advisor.support.IEventExceptionTranslator;

/**
 * Constants related to determining which {@link EventType EventType} to use for Spring- related {@link Event Event}s.
 *
 * @author Christophe Friederich
 * @since 1.3
 */
public final class SpringEventType {

    /**
     * Defines the {@code init-param} which may be used for controlling whether an event is added when a portion of
     * Spring initialisation is bypassed due to previous errors.
     * <p/>
     * Note: This flag does not control whether an event is added when Spring initialisation is not bypassed and fails.
     *
     * @see #addEventOnBypass(javax.servlet.ServletContext)
     */
    public static final String PARAM_ADD_EVENT_ON_BYPASS = "event.spring.addEventOnBypass";

    /**
     * Defines the {@code init-param} which may be used for controlling the event type added when Spring events occur.
     * Where the value must be set depends on the type being initialised.
     *
     * @see #getContextEventType(javax.servlet.ServletContext)
     * @see #getServletEventType(javax.servlet.ServletConfig)
     */
    public static final String PARAM_EVENT_TYPE = "event.spring.eventType";

    /**
     * An {@code init-param} which can be applied to the {@code ServletContext} or {@code ServletConfig} to register one
     * or more {@link IEventExceptionTranslator} types to be applied to exceptions thrown from Spring or SpringMVC
     * startup.
     *
     * @see #translateThrowable(ServletConfig, Throwable)
     */
    public static final String PARAM_EXCEPTION_TRANSLATOR_CLASS = "exceptionTranslatorClass";

    /**
     * Defines the separator characters which can be used between {@link #PARAM_EXCEPTION_TRANSLATOR_CLASS exception
     * translator classes}.
     */
    public static final String SEPARATORS = ",; \t\n";

    /**
     * Defines the default context event type which will be used if one is not explicitly set.
     */
    public static final String SPRING_CONTEXT_EVENT_TYPE = "spring";

    /**
     * Defines the default servlet event type which will be used if one is not explicitly set.
     */
    public static final String SPRING_SERVLET_EVENT_TYPE = "spring-mvc";

    /** */
    private static final Logger LOG = LoggerFactory.getLogger(SpringEventType.class);

    private SpringEventType() {
        throw new UnsupportedOperationException(getClass().getName() + " should not be instantiated");
    }

    /**
     * Retrieves a flag indicating whether a event should be added when Spring initialisation is bypassed due to
     * previous fatal errors.
     * <p/>
     * By default, an event is <i>not</i> added. If a {@code context-param} named {@link #PARAM_ADD_EVENT_ON_BYPASS}
     * exists with the value {@code true}, then an {@link #getContextEventType(javax.servlet.ServletContext) event} will
     * be added when initialisation is bypassed.
     * <p/>
     * To set this value, add the following to {@code web.xml}:
     *
     * <pre>
     * <code>
     *     &lt;context-param&gt;
     *         &lt;param-name&gt;event.spring.addEventOnBypass&lt;/param-name&gt;
     *         &lt;param-value&gt;true&lt;/param-value&gt;
     *     &lt;/context-param&gt;
     * </code>
     * </pre>
     *
     * Note: If initialisation is not bypassed and fails, this flag <i>does not</i> control whether an event will be
     * added at that time.
     *
     * @param context
     *            the servlet context
     * @return {@code true} if an event has been explicitly requested; otherwise, {@code false}
     */
    public static boolean addEventOnBypass(@Nonnull final ServletContext context) {
        return "true".equals(checkNotNull(context, "context").getInitParameter(PARAM_ADD_EVENT_ON_BYPASS));
    }

    /**
     * Retrieves a flag indicating whether a event should be added when SpringMVC initialisation is bypassed due to
     * previous fatal Spring errors.
     * <p/>
     * By default, an event is <i>not</i> added. If an {@code init-param} named {@link #PARAM_ADD_EVENT_ON_BYPASS}
     * exists, its value ({@code true} or {@code false}) controls whether an event is added. Otherwise, a fallback check
     * is made {@link #addEventOnBypass(javax.servlet.ServletContext) to the context} for a {@code context-param}. This
     * means if an event is explicitly requested at the context level, by default it will also be requested at the
     * servlet level. However, individual servlets can explicitly disable that by setting their {@code init-param} to
     * {@code false}.
     * <p/>
     * To set this value, add the following to the declaration for the servlet in {@code web.xml}:
     *
     * <pre>
     * <code>
     *     &lt;init-param&gt;
     *         &lt;param-name&gt;event.spring.addEventOnBypass&lt;/param-name&gt;
     *         &lt;param-value&gt;true&lt;/param-value&gt;
     *     &lt;/init-param&gt;
     * </code>
     * </pre>
     *
     * Note: If initialisation is not bypassed and fails, this flag <i>does not</i> control whether an event will be
     * added at that time.
     *
     * @param config
     *            the servlet configuration
     * @return {@code true} if an event has been specifically requested, either at the servlet level or at the context
     *         level; otherwise, {@code false}
     */
    public static boolean addEventOnBypass(@Nonnull final ServletConfig config) {
        final String value = checkNotNull(config, "config").getInitParameter(PARAM_ADD_EVENT_ON_BYPASS);
        if (value == null) {
            // If no parameter was found at the servlet level, look at the context level.
            return addEventOnBypass(config.getServletContext());
        }
        // If any value is found at the servlet level, be it true or false, that value always overrides any value set
        // at the context level.
        return "true".equals(value);
    }

    /**
     * A fail-safe event creator with reliable semantics to fall back on when a more specific {@link Event event} is not
     * available.
     *
     * @param eventType
     *            the event type to use
     * @param message
     *            the message to use
     * @param t
     *            the exception thrown while attempting to initialise the WebApplicationContext
     * @return the event to add, which will never be {@code null}
     */
    @Nonnull
    public static Event createDefaultEvent(@Nonnull final String eventType,
        @Nonnull final String message,
        @Nonnull final Throwable t) {
        final IEventConfig config = ServletEventAdvisor.getInstance().getConfig();
        return new Event(config.getEventType(eventType), message, Event.toString(t),
                config.getEventLevel(EventLevel.FATAL));
    }

    /**
     * Examines the provided {@code ServletContext} for a {@code context-param} named {@link #PARAM_EVENT_TYPE} and, if
     * one is found, returns its value; otherwise the default {@link #SPRING_CONTEXT_EVENT_TYPE} is returned.
     * <p/>
     * To set this value, add the following to {@code web.xml}:
     *
     * <pre>
     * <code>
     *     &lt;context-param&gt;
     *         &lt;param-name&gt;event.spring.eventType&lt;/param-name&gt;
     *         &lt;param-value&gt;my-spring-context-event-type&lt;/param-value&gt;
     *     &lt;/context-param&gt;
     * </code>
     * </pre>
     *
     * @param context
     *            the servlet context
     * @return the context event type
     */
    @Nonnull
    public static String getContextEventType(@Nonnull final ServletContext context) {
        String value = checkNotNull(context, "context").getInitParameter(PARAM_EVENT_TYPE);
        if (!StringUtils.hasText(value)) {
            value = SPRING_CONTEXT_EVENT_TYPE;
        }
        return value;
    }

    /**
     * Examines the provided {@code ServletConfig} for an {@code init-param} named {@link #PARAM_EVENT_TYPE} and, if one
     * is found, returns its value; otherwise, the default {@link #SPRING_SERVLET_EVENT_TYPE} is returned.
     * <p/>
     * To set this value, add the following to the declaration for the servlet in {@code web.xml}:
     *
     * <pre>
     * <code>
     *     &lt;init-param&gt;
     *         &lt;param-name&gt;event.spring.eventType&lt;/param-name&gt;
     *         &lt;param-value&gt;my-spring-servlet-event-type&lt;/param-value&gt;
     *     &lt;/init-param&gt;
     * </code>
     * </pre>
     *
     * @param config
     *            the servlet configuration
     * @return the servlet event type
     */
    @Nonnull
    public static String getServletEventType(@Nonnull final ServletConfig config) {
        String value = checkNotNull(config, "config").getInitParameter(PARAM_EVENT_TYPE);
        if (!StringUtils.hasText(value)) {
            value = SPRING_SERVLET_EVENT_TYPE;
        }
        return value;
    }

    @Nullable
    public static Event translateThrowable(@Nonnull final ServletConfig config, @Nonnull final Throwable t) {
        return translateThrowable(new ServletConfigMapSupplier(config), t);
    }

    @Nullable
    public static Event translateThrowable(@Nonnull final ServletContext servletContext, @Nonnull final Throwable t) {
        return translateThrowable(new ServletContextMapSupplier(servletContext), t);
    }

    @Nullable
    @SuppressWarnings("unchecked")
    private static Class<IEventExceptionTranslator> loadTranslatorClass(@Nonnull final String className) {
        try {
            final Class<?> clazz = ClassUtils.forName(className, ClassUtils.getDefaultClassLoader());
            if (IEventExceptionTranslator.class.isAssignableFrom(clazz)) {
                return (Class<IEventExceptionTranslator>) clazz;
            }
            LOG.warn("Translator class {} does not implement {}", className, IEventExceptionTranslator.class.getName());
        } catch (final ClassNotFoundException e) {
            LOG.warn("Translator class {} could not be loaded", className);
        }

        return null;
    }

    private static Event translateThrowable(@Nonnull final MapSupplier supplier, @Nonnull final Throwable t) {
        final String param = supplier.getValue(PARAM_EXCEPTION_TRANSLATOR_CLASS);
        if (!StringUtils.hasText(param)) {
            return null;
        }

        final List<Class<IEventExceptionTranslator>> translatorClasses = new ArrayList<>();
        for (final String className : StringUtils.tokenizeToStringArray(param, SEPARATORS)) {
            final Class<IEventExceptionTranslator> clazz = loadTranslatorClass(className);
            if (clazz != null) {
                translatorClasses.add(clazz);
            }
        }

        if (translatorClasses.isEmpty()) {
            LOG.warn("None of the configured translator classes could be loaded");
            return null;
        }

        for (final Class<IEventExceptionTranslator> clazz : translatorClasses) {
            try {
                final IEventExceptionTranslator translator = BeanUtils.instantiateClass(clazz,
                    IEventExceptionTranslator.class);
                if (translator instanceof IConfigurable) {
                    ((IConfigurable) translator).init(supplier.get());
                }

                final Event event = translator.translate(t);
                if (event != null) {
                    return event;
                }
            } catch (final BeanInstantiationException e) {
                LOG.warn("{} could not be instantiated", clazz.getName(), e);
            }
        }
        return null;
    }

    // If you're wondering, everything that happens below here is because ServletConfig and ServletContext have a
    // set of _completely identical methods_ but no shared interface between them. Because Java.

    private abstract static class AbstractMapSupplier implements MapSupplier {

        private Map<String, String> map;

        @Nonnull
        @Override
        public Map<String, String> get() {
            if (map == null) {
                map = buildMap();
            }

            return map;
        }

        protected Map<String, String> buildMap() {
            final List<String> names = Collections.list(getNames());
            if (names.isEmpty()) {
                return Collections.emptyMap();
            }

            final Map<String, String> map = new HashMap<>(names.size(), 1.0f);
            for (final String name : names) {
                map.put(name, getValue(name));
            }
            return Collections.unmodifiableMap(map);
        }

        protected abstract Enumeration<String> getNames();
    }

    /**
     * @author Christophe Friederich
     * @since 1.3
     */
    private interface MapSupplier {

        @Nonnull
        Map<String, String> get();

        @Nullable
        String getValue(@Nonnull String name);
    }

    /**
     * @author Christophe Friederich
     * @since 1.3
     */
    private static final class ServletConfigMapSupplier extends AbstractMapSupplier {

        private final ServletConfig config;

        private ServletConfigMapSupplier(final ServletConfig config) {
            this.config = config;
        }

        @Override
        public String getValue(@Nonnull final String name) {
            return config.getInitParameter(name);
        }

        @Nonnull
        @Override
        protected Enumeration<String> getNames() {
            return config.getInitParameterNames();
        }
    }

    /**
     * @author Christophe Friederich
     * @since 1.3
     */
    private static final class ServletContextMapSupplier extends AbstractMapSupplier {

        /** */
        private final ServletContext servletContext;

        private ServletContextMapSupplier(final ServletContext servletContext) {
            this.servletContext = servletContext;
        }

        @Override
        public String getValue(@Nonnull final String name) {
            return servletContext.getInitParameter(name);
        }

        @Override
        protected Enumeration<String> getNames() {
            return servletContext.getInitParameterNames();
        }
    }
}
