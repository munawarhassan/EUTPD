package com.pmi.tpd.core.event.advisor.servlet;

import javax.annotation.Nonnull;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import com.pmi.tpd.core.event.advisor.EventAdvisorService;

// CHECKSTYLE:OFF
/**
 * Initialises and terminates Event mgt with the servlet container.
 * <p/>
 * In web environments, this is the preferred mechanism for managing lifecycle, allowing it to be initialised and
 * terminated in a thread-safe context. This listener should be registered before any others except, if applicable,
 * those that setup logging.
 * <p/>
 * To use this listener, add the following to web.xml:
 *
 * <pre>
 * <code>&lt;listener&gt;
 *     &lt;listener-class&gt;com.pmi.tpd.core.event.system.spi.EventServletContextListener&lt;/listener-class&gt;
 * &lt;/listener&gt;
 * </code>
 * </pre>
 *
 * Alternatively, if the application is deployed in a Servlet 3 container such as Tomcat 7, this listener can be
 * registered in a {@code ServletContainerInitializer} using {@code ServletContext.addListener}.
 *
 * @author Christophe Friederich
 * @since 1.3
 */
// CHECKSTYLE:ON
public class ServletEventContextListener implements ServletContextListener {

    /**
     * When a {@code ServletEventContextListener} {@link #register(ServletContext) is registered} with a
     * {@code ServletContext}, this attribute will be used to mark its registration so that multiple listeners will not
     * be registered.
     *
     * @since 3.0
     */
    public static final String ATTR_REGISTERED = ServletEventContextListener.class.getName() + ":Registered";

    /**
     * Registers a {@code EventServletContextListener} with the provided {@code ServletContext} if such a listener has
     * not already been registered.
     * <p/>
     * This method is for use as part of Servlet 3-style {@code ServletContainerInitializer} initialization. Listeners
     * cannot be added to {@code ServletContext} instances after they have already been initialized.
     *
     * @param context
     *            the {@code ServletContext} to register a {@code EventServletContextListener} with
     * @throws IllegalArgumentException
     *             if the provided {@code ServletContext} was not passed to
     *             {@code ServletContainerInitializer#onStartup(Set, ServletContext)}
     * @throws IllegalStateException
     *             if the {@code ServletContext} has already been initialized
     * @throws UnsupportedOperationException
     *             if this {@code ServletContext} was passed to the
     *             {@code ServletContextListener#contextInitialized(ServletContextEvent)} method of a
     *             {@code ServletContextListener} that was not either declared in {@code web.xml} or
     *             {@code web-fragment.xml} or annotated with {@code &#064;WebListener}
     */
    public static void register(@Nonnull final ServletContext context) {
        if (context.getAttribute(ATTR_REGISTERED) == null) {
            context.addListener(new ServletEventContextListener());

            context.setAttribute(ATTR_REGISTERED, Boolean.TRUE);
        }
    }

    /**
     * Terminates {@link EventAdvisorService}.
     *
     * @param event
     *            the context event
     * @see EventAdvisorService#terminate(Object)
     */
    @Override
    public void contextDestroyed(@Nonnull final ServletContextEvent event) {
        ServletEventAdvisor.getInstance().terminate(event.getServletContext());
    }

    /**
     * Initialises {@link EventAdvisorService}.
     *
     * @param event
     *            the context event
     * @see EventAdvisorService#initialize(Object)
     */
    @Override
    public void contextInitialized(@Nonnull final ServletContextEvent event) {
        ServletEventAdvisor.initialize(event.getServletContext());
    }
}
