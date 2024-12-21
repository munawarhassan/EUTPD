package com.pmi.tpd.core.event.advisor.spring.web.context.support;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import org.springframework.web.context.AbstractContextLoaderInitializer;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.WebApplicationContext;

import com.pmi.tpd.core.event.advisor.servlet.ServletEventContextListener;
import com.pmi.tpd.core.event.advisor.spring.web.context.EventContextLoaderListener;

/**
 * Extends Spring's {@code AbstractDispatcherServletInitializer} to use event-aware components.
 * <ul>
 * <li>A {@link ServletEventContextListener} will be registered <i>before</i> any other listeners that are registered by
 * this initializer</li>
 * <li>A {@link EventContextLoaderListener} will be used to initialize the {@link #createRootApplicationContext() root
 * ApplicationContext}</li>
 * </ul>
 * <p/>
 * In addition to using event-aware components by default, this base class allows derived initializers to override
 * {@link #createContextLoaderListener(WebApplicationContext) the ContextLoaderListener} used. This is intended to allow
 * for application-specific handling on top of the event-aware handling.
 *
 * @author Christophe Friederich
 * @since 1.3
 */
public abstract class AbstractEventContextLoaderInitializer extends AbstractContextLoaderInitializer {

    /**
     * Creates a {@link EventContextLoaderListener} which will initialize and terminate the provided
     * {@code WebApplicationContext}. This method is provided as a convenience for derived classes to simplify replacing
     * the listener used.
     *
     * @param context
     *            the {@code WebApplicationContext} to be initialized by the created listener
     * @return the listener to register with the {@code ServletContext}
     */
    protected ContextLoaderListener createContextLoaderListener(final WebApplicationContext context) {
        return new EventContextLoaderListener(context);
    }

    /**
     * {@link #registerEventContextListener(ServletContext) Registers a} {@link ServletEventContextListener} and then
     * delegates to the superclass's {@code onStartup(ServletContext)} implementation.
     *
     * @param servletContext
     *            the {@code ServletContext} to initialize
     * @throws ServletException
     *             potentially thrown by the superclass {@code onStartup(ServletContext)} implementation
     */
    @Override
    public void onStartup(final ServletContext servletContext) throws ServletException {
        registerEventContextListener(servletContext);

        super.onStartup(servletContext);
    }

    /**
     * Overrides {@code AbstractContextLoaderInitializer}'s {@code registerContextLoaderListener} to register a
     * {@link #createContextLoaderListener(WebApplicationContext) ServletEventContextListener} instead of the standard
     * Spring {@code ContextLoaderListener}.
     *
     * @param servletContext
     *            the {@code ServletContext} to register the {@link EventContextLoaderListener} in
     */
    @Override
    protected void registerContextLoaderListener(final ServletContext servletContext) {
        final WebApplicationContext context = createRootApplicationContext();
        if (context == null) {
            logger.debug("No ContextLoaderListener registered, "
                    + "as createRootApplicationContext() did not return an application context");
        } else {
            servletContext.addListener(createContextLoaderListener(context));
        }
    }

    /**
     * Registers an {@link ServletEventContextListener} in in the provided {@code ServletContext}. This listener ensures
     * Event-application is initialized and terminated with the application.
     * <p/>
     * Note: Even if this method is called multiple times, with its default implementation the listener will only be
     * added <i>once</i>.
     *
     * @param servletContext
     *            the {@code ServletContext} to register the {@link ServletEventContextListener} in
     * @see ServletEventContextListener#register(ServletContext)
     */
    protected void registerEventContextListener(final ServletContext servletContext) {
        ServletEventContextListener.register(servletContext);
    }
}
