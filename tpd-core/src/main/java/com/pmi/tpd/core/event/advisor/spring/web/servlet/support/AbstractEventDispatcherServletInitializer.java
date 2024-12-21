package com.pmi.tpd.core.event.advisor.spring.web.servlet.support;

import javax.servlet.Filter;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;

import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.support.AbstractDispatcherServletInitializer;

import com.pmi.tpd.core.event.advisor.servlet.ServletEventContextListener;
import com.pmi.tpd.core.event.advisor.spring.web.context.EventContextLoaderListener;
import com.pmi.tpd.core.event.advisor.spring.web.servlet.EventDispatcherServlet;

/**
 * Extends Spring's {@code AbstractDispatcherServletInitializer} to use event-aware components.
 * <ul>
 * <li>A {@link ServletEventContextListener} will be registered <i>before</i> any other listeners that are registered by
 * this initializer</li>
 * <li>A {@link EventContextLoaderListener} will be used to initialize the {@link #createRootApplicationContext() root
 * ApplicationContext}, if one is created</li>
 * <li>A {@link EventDispatcherServlet} will be used to initialize the {@link #createServletApplicationContext() servlet
 * ApplicationContext}</li>
 * </ul>
 * <p/>
 * In addition to using event-aware components by default, this base class allows derived initializers to override
 * {@link #createContextLoaderListener(WebApplicationContext) the ContextLoaderListener} and
 * {@link #createDispatcherServlet(WebApplicationContext) DispatcherServlet} types used. This is intended to allow for
 * application-specific handling on top of the event-aware handling.
 *
 * @author Christophe Friederich
 * @since 1.3
 */
public abstract class AbstractEventDispatcherServletInitializer extends AbstractDispatcherServletInitializer {

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
     * Creates a {@link EventDispatcherServlet}, which will initialize the SpringMVC context in a Event-aware away. If
     * SpringMVC initialization fails, the application will be locked.
     *
     * @param context
     *            the {@code WebApplicationContext} to be initialized by the created dispatcher
     * @return the servlet to register with the {@code ServletContext}
     */
    protected DispatcherServlet createDispatcherServlet(final WebApplicationContext context) {
        return new EventDispatcherServlet(context);
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
     * Overrides {@code AbstractContextLoaderListener}'s {@code registerContextLoaderListener} to register a
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
     * Overrides {@code AbstractDispatcherServletInitializer}'s {@code registerDispatcherServlet(ServletContext)} to
     * register a {@link #createDispatcherServlet(WebApplicationContext) EventDispatcherServlet} instead of the standard
     * Spring {@code DispatcherServlet}.
     *
     * @param servletContext
     *            the {@code ServletContext} to register the {@link EventDispatcherServlet} in
     */
    @Override
    protected void registerDispatcherServlet(final ServletContext servletContext) {
        final String servletName = getServletName();
        Assert.hasLength(servletName, "getServletName() may not return empty or null");

        final WebApplicationContext servletAppContext = createServletApplicationContext();
        Assert.notNull(servletAppContext,
            "createServletApplicationContext() did not return an application context for servlet [" + servletName
                    + "]");

        final ServletRegistration.Dynamic registration = servletContext.addServlet(servletName,
            createDispatcherServlet(servletAppContext));
        Assert.notNull(registration, "Failed to register servlet with name '" + servletName
                + "'. Check if there is another servlet registered under the same name.");

        registration.setAsyncSupported(isAsyncSupported());
        registration.setLoadOnStartup(1);
        registration.addMapping(getServletMappings());

        final Filter[] filters = getServletFilters();
        if (!ObjectUtils.isEmpty(filters)) {
            for (final Filter filter : filters) {
                registerServletFilter(servletContext, filter);
            }
        }

        customizeRegistration(registration);
    }

    /**
     * Registers an {@link ServletEventContextListener} in in the provided {@code ServletContext}. This listener ensures
     * event is initialized and terminated with the application.
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
