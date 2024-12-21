package com.pmi.tpd.core.event.advisor.spring.web.context;

import static com.pmi.tpd.core.event.advisor.spring.web.SpringEventType.createDefaultEvent;
import static com.pmi.tpd.core.event.advisor.spring.web.SpringEventType.translateThrowable;

import javax.annotation.Nonnull;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Conventions;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.WebApplicationContext;

import com.pmi.tpd.api.event.advisor.EventLevel;
import com.pmi.tpd.api.event.advisor.IEventContainer;
import com.pmi.tpd.api.event.advisor.event.Event;
import com.pmi.tpd.api.event.advisor.event.IApplicationEventCheck;
import com.pmi.tpd.core.event.advisor.config.IEventConfig;
import com.pmi.tpd.core.event.advisor.servlet.ServletEventAdvisor;
import com.pmi.tpd.core.event.advisor.spring.web.SpringEventType;

/**
 * Extends the standard Spring {@code ContextLoaderListener} to make it event-aware. When using this class, if the
 * Spring context fails to start an event will added to event rather than propagated to the servlet container.
 * <p/>
 * The goal of this class is to prevent the web application from being shutdown if Spring cannot be started. By default,
 * if the {@code WebApplicationContext} cannot be started for any reason, an exception is thrown which is propagated up
 * to the container. When this happens, the entire web application is terminated. This precludes the use, which requires
 * that the web application be up so that it can serve its status pages.
 *
 * @author Christophe Friederich
 * @since 1.3
 */
public class EventContextLoaderListener extends ContextLoaderListener {

    /**
     * The attribute added to the {@code ServletContext} when Spring context initialization is bypassed because a
     * previous {@link Event} indicates the application has already failed.
     */
    public static final String ATTR_BYPASSED = Conventions.getQualifiedAttributeName(EventContextLoaderListener.class,
        "bypassed");

    /** */
    private static final Logger LOGGER = LoggerFactory.getLogger(EventContextLoaderListener.class);

    public EventContextLoaderListener() {
    }

    public EventContextLoaderListener(final WebApplicationContext context) {
        super(context);
    }

    /**
     * Performs standard Spring {@code ContextLoaderListener} teardown and ensures any attributes added to the servlet
     * context for this dispatcher are removed.
     *
     * @param event
     *              the context event
     */
    @Override
    public void contextDestroyed(final ServletContextEvent event) {
        try {
            super.contextDestroyed(event);
        } finally {
            event.getServletContext().removeAttribute(ATTR_BYPASSED);
        }
    }

    /**
     * Overrides the standard Spring {@code ContextLoaderListener} initialisation to make it event-aware, allowing it to
     * be automatically bypassed, when {@link IApplicationEventCheck application checks} produce {@link EventLevel#FATAL
     * fatal} events, or add an {@link Event} if initialisation fails.
     * <p/>
     * This implementation will never throw an exception. Unlike the base implementation, though, it may return
     * {@code null} if initialisation is bypassed (due to previous fatal events or failing to initialise) or if
     * initialisation fails.
     *
     * @param servletContext
     *                       the servlet context
     * @return the initialised context, or {@code null} if initialisation is bypassed or fails
     */
    @Override
    public WebApplicationContext initWebApplicationContext(final ServletContext servletContext) {
        final String eventType = SpringEventType.getContextEventType(servletContext);

        // Search for previous FATAL errors and, if any are found, add another indicating Spring startup has been
        // canceled. The presence of other error types in the container (warnings, errors) will not prevent this
        // implementation from attempting to start Spring.
        final IEventContainer container = ServletEventAdvisor.getInstance().getEventContainer(servletContext);
        if (container.hasEvents()) {
            LOGGER.debug("Searching Event for previous {} errors", EventLevel.FATAL);
            for (final Event event : container.getEvents()) {
                final EventLevel level = event.getLevel();
                if (EventLevel.FATAL.equals(level.getLevel())) {
                    LOGGER.error(
                        "Bypassing Spring ApplicationContext initialisation; a previous {} error was found: {}",
                        level.getLevel(),
                        event.getDesc());
                    servletContext.setAttribute(ATTR_BYPASSED, Boolean.TRUE);

                    if (SpringEventType.addEventOnBypass(servletContext)) {
                        final String message = "The Spring WebApplicationContext will not be started due to a previous "
                                + level.getLevel() + " error";
                        final IEventConfig config = ServletEventAdvisor.getInstance().getConfig();
                        container.publishEvent(new Event(config.getEventType(eventType), message, level));
                    }

                    // The base class actually ignores the return, so null is safe.
                    return null;
                }
            }
        }

        WebApplicationContext context = null;
        try {
            LOGGER.debug("Attempting to initialise the Spring ApplicationContext");
            context = super.initWebApplicationContext(servletContext);
        } catch (final Throwable t) {
            final String message = "The Spring WebApplicationContext could not be started";
            LOGGER.error(message, t);

            // The Spring ContextLoader class sets the exception that was thrown during initialisation on the servlet
            // context under this constant. Whenever things attempt to retrieve the WebApplicationContext from the
            // servlet context, if the property value is an exception, it is rethrown. This makes other parts of the
            // web application, like DelegatingFilterProxies, fail to start (which, in turn, brings down the entire
            // web application and prevents access to Event).
            // Because we need the web application to be able to come up even if Spring fails, that behaviour is not
            // desirable. So before we add the event to Event, the first thing we do is clear that attribute back
            // off of the context. That way, when things attempt to retrieve the context, they'll just get a null back
            // (which matches what happens if we bypass Spring startup completely, above)
            servletContext.removeAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE);

            // After we remove Spring's attribute, we need to set our own. This allows other Event-aware constructs
            // to know we've bypassed Spring initialisation (or, more exactly, that it failed)
            Event event = translateThrowable(servletContext, t); // First apply EventExceptionTranslators, if set
            if (event == null) {
                event = createEvent(eventType, message, t); // For 2.x compatibility, try createEvent
            }
            servletContext.setAttribute(ATTR_BYPASSED, event); // event is never null by this point

            // Add the event
            container.publishEvent(event);
        }
        return context;
    }

    /**
     * May be overridden in derived classes to allow them to override the default event type or message based on
     * application-specific understanding of the exception that was thrown.
     * <p/>
     * For cases where derived classes are not able to offer a more specific event type or message, they are encouraged
     * to fall back on the behaviour of this superclass method.
     *
     * @param defaultEventType
     *                         the default event type to use if no more specific type is appropriate
     * @param defaultMessage
     *                         the default message to use if no more specific message is available
     * @param t
     *                         the exception thrown while attempting to initialise the WebApplicationContext
     * @return the event to add to, which may not be {@code null}
     */
    @Nonnull
    protected Event createEvent(@Nonnull final String defaultEventType,
        @Nonnull final String defaultMessage,
        @Nonnull final Throwable t) {
        return createDefaultEvent(defaultEventType, defaultMessage, t);
    }
}
