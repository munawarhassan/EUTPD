package com.pmi.tpd.core.event.advisor.spring.web.servlet;

import static com.pmi.tpd.core.event.advisor.spring.web.SpringEventType.createDefaultEvent;
import static com.pmi.tpd.core.event.advisor.spring.web.SpringEventType.translateThrowable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Conventions;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

import com.pmi.tpd.api.event.advisor.EventLevel;
import com.pmi.tpd.api.event.advisor.IEventContainer;
import com.pmi.tpd.api.event.advisor.event.Event;
import com.pmi.tpd.core.event.advisor.config.IEventConfig;
import com.pmi.tpd.core.event.advisor.servlet.ServletEventAdvisor;
import com.pmi.tpd.core.event.advisor.spring.web.SpringEventType;
import com.pmi.tpd.core.event.advisor.spring.web.context.EventContextLoaderListener;

/**
 * Extends the standard Spring {@code DispatcherServlet} to make it event-aware. When using this class, if the root
 * Spring context fails to start the dispatcher will not attempt to create/start its child context.
 * <p/>
 * The goal of this class is to prevent the web application from being shutdown if SpringMVC cannot be started. By
 * default, if the dispatcher's {@code WebApplicationContext} cannot be started for any reason, an exception is thrown
 * which is propagated up to the container. When this happens, the entire web application is terminated. This precludes
 * the use, which requires that the web application be up so that it can serve its status pages.
 *
 * @author Christophe Friederich
 * @since 1.3
 */
public class EventDispatcherServlet extends DispatcherServlet {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    /**
     * The prefix for the attribute added to the {@code ServletContext} when Spring MVC initialization is bypassed
     * because a previous {@link Event} indicates the application has already failed. The MVC dispatcher's name is
     * appended to this prefix, separated by a colon, to form the attribute.
     *
     * @see #getBypassedAttributeName()
     */
    public static final String PREFIX_BYPASSED = Conventions.getQualifiedAttributeName(EventDispatcherServlet.class,
        "bypassed");

    /** */
    private static final Logger LOGGER = LoggerFactory.getLogger(EventDispatcherServlet.class);

    /**
     *
     */
    public EventDispatcherServlet() {
    }

    /**
     * @param webApplicationContext
     */
    public EventDispatcherServlet(final WebApplicationContext webApplicationContext) {
        super(webApplicationContext);
    }

    /**
     * Performs standard SpringMVC {@code DispatcherServlet} teardown and ensures any attributes added to the servlet
     * context for this dispatcher are removed.
     */
    @Override
    public void destroy() {
        try {
            super.destroy();
        } finally {
            getServletContext().removeAttribute(getBypassedAttributeName());
        }
    }

    /**
     * Overrides the standard SpringMVC {@code DispatcherServlet} initialisation to make it event-aware, allowing it to
     * be automatically bypassed, when paired with a {@link EventContextLoaderListener}, or add an {@link Event} if
     * initialisation fails.
     * <p/>
     * This implementation will never throw an exception. Unlike the base implementation, though, it may return
     * {@code null} if initialisation is bypassed (due to the main {@code WebApplicationContext} being bypassed or
     * failing to initialise) or if initialisation fails.
     *
     * @return the initialised context, or {@code null} if initialisation is bypassed or fails
     */
    @Override
    @Nullable
    protected WebApplicationContext initWebApplicationContext() {
        final ServletConfig servletConfig = getServletConfig();
        final ServletContext servletContext = getServletContext();
        final IEventContainer container = ServletEventAdvisor.getInstance().getEventContainer(servletContext);
        final String eventType = SpringEventType.getServletEventType(servletConfig);

        final Object attribute = servletContext.getAttribute(EventContextLoaderListener.ATTR_BYPASSED);
        // First, check to see if the WebApplicationContext was bypassed. If it was, it's possible, based on
        // configuration, that no event was added. However, we must bypass SpringMVC initialisation as well,
        // because no parent context will be available.
        if (Boolean.TRUE == attribute) {
            // Fully bypassed, without even trying to start
            LOGGER.error("Bypassing SpringMVC dispatcher [{}] initialisation; Spring initialisation was bypassed",
                getServletName());
            servletContext.setAttribute(getBypassedAttributeName(), Boolean.TRUE);

            if (SpringEventType.addEventOnBypass(servletConfig)) {
                final String message = "SpringMVC dispatcher [" + getServletName()
                        + "] will not be started because the Spring WebApplicationContext was not started";
                final IEventConfig config = ServletEventAdvisor.getInstance().getConfig();
                container.publishEvent(
                    new Event(config.getEventType(eventType), message, config.getEventLevel(EventLevel.FATAL)));
            }

            return null;
        }
        // If WebApplicationContext initialisation wasn't bypassed, check to see if it failed. SpringMVC initialisation
        // is guaranteed to fail if the primary WebApplicationContext failed, so we'll want to bypass it.
        if (attribute instanceof Event) {
            final Event event = (Event) attribute;

            LOGGER.error("Bypassing SpringMVC dispatcher [{}] initialisation; Spring initialisation failed: {}",
                getServletName(),
                event.getDesc());
            servletContext.setAttribute(getBypassedAttributeName(), Boolean.TRUE);

            if (SpringEventType.addEventOnBypass(servletConfig)) {
                final String message = "SpringMVC dispatcher [" + getServletName()
                        + "] will not be started because the Spring WebApplicationContext failed to start";
                final IEventConfig config = ServletEventAdvisor.getInstance().getConfig();
                container.publishEvent(new Event(config.getEventType(eventType), message, event.getLevel()));
            }

            return null;
        }

        // If we make it here, the Spring WebApplicationContext should have started successfully. That means it's safe
        // to try and start this SpringMVC dispatcher.
        WebApplicationContext context = null;
        try {
            LOGGER.debug("Attempting to initialise the Spring ApplicationContext");
            context = super.initWebApplicationContext();
        } catch (final Throwable t) {
            final String message = "SpringMVC dispatcher [" + getServletName() + "] could not be started";
            LOGGER.error(message, t);

            Event event = translateThrowable(getServletConfig(), t); // First apply EventExceptionTranslators, if set
            if (event == null) {
                event = createEvent(eventType, message, t); // For 2.x compatibility, try createEvent
            }
            servletContext.setAttribute(getBypassedAttributeName(), event);

            container.publishEvent(event);
            throw t;
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
     * @return the event to add, which may not be {@code null}
     */
    @Nonnull
    protected Event createEvent(@Nonnull final String defaultEventType,
        @Nonnull final String defaultMessage,
        @Nonnull final Throwable t) {
        return createDefaultEvent(defaultEventType, defaultMessage, t);
    }

    /**
     * Allows derived classes to override the name of the attribute which is added to the {@code ServletContext} when
     * this dispatcher is bypassed (whether due to the {@link EventContextLoaderListener} being bypassed or due to the
     * initialisation of the dispatcher failing).
     * <p/>
     * The default attribute name is: "{@link #PREFIX_BYPASSED}:{@code getServletName()}"
     *
     * @return the name for the bypassed context attribute
     */
    @Nonnull
    protected String getBypassedAttributeName() {
        return PREFIX_BYPASSED + ":" + getServletName();
    }
}
