package com.pmi.tpd.core.event.advisor.spring.lifecycle;

import java.io.IOException;
import java.net.URLEncoder;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

import com.pmi.tpd.core.event.advisor.EventAdvisorService;
import com.pmi.tpd.core.event.advisor.config.IEventConfig;
import com.pmi.tpd.core.event.advisor.spring.web.servlet.EventDispatcherServlet;

/**
 * Initializes SpringMVC on a separate thread, allowing the web application to handle basic requests while the
 * application is still starting.
 * <p/>
 * SpringMVC is started <i>standalone</i> by this servlet. It is assumed that no {@code ContextLoaderListener} has been
 * registered and prepared a root {@code WebApplicationContext} and that the SpringMVC context should be used as the
 * root.
 * <p/>
 * When using this servlet, {@link LifecycleDelegatingFilterProxy} and {@link LifecycleHttpRequestHandlerServlet} should
 * be used in place of Spring's {@code DelegatingFilterProxy} and {@code HttpRequestHandlerServlet}. The lifecycle
 * versions automatically defer attempting to bind to their associated beans until the application has
 * {@link LifecycleState#STARTED started}.
 *
 * @author Christophe Friederich
 * @since 1.3
 */
public class LifecycleDispatcherServlet extends HttpServlet {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    /** */
    private static final String PROP_SYNCHRONOUS = "event.spring.lifecycle.synchronousStartup";

    /** */
    private final Logger log = LoggerFactory.getLogger(getClass());

    /** */
    private volatile DispatcherServlet delegate;

    /** */
    private WebApplicationContext applicationContext;

    /** */
    private volatile Thread startup;

    /** */
    private volatile ConfigurableEnvironment environment;

    /**
     *
     */
    public LifecycleDispatcherServlet() {
    }

    /**
     * @param applicationContext
     */
    public LifecycleDispatcherServlet(final WebApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    /**
     * @param environment
     */
    public void setEnvironment(final ConfigurableEnvironment environment) {
        this.environment = environment;
    }

    @Override
    public void destroy() {
        final Thread startup = this.startup; // Shadowing intentional
        if (startup != null) {
            if (startup.isAlive()) {
                // Try to interrupt Spring startup so that the server can terminate
                startup.interrupt();
            }

            try {
                // Wait for Spring startup to either complete or bomb out due to being interrupted. Note that,
                // because Spring may still complete its startup despite our attempt to interrupt it, we check
                // the delegate DispatcherServlet after cleaning up the startup thread
                startup.join();
            } catch (final InterruptedException e) {
                log.error("The SpringMVC startup thread could not be joined", e);
            }
        }

        final DispatcherServlet delegate = this.delegate; // Shadowing intentional
        if (delegate != null) {
            // Shut down Spring
            delegate.destroy();
        }
    }

    @Override
    public void init() throws ServletException {
        final Thread thread = new Thread(new Runnable() {

            @Override
            public void run() {
                @SuppressWarnings("serial")
                final EventDispatcherServlet servlet = new EventDispatcherServlet(applicationContext) {

                    @Override
                    public ConfigurableEnvironment getEnvironment() {
                        if (LifecycleDispatcherServlet.this.environment == null) {
                            super.getEnvironment();
                        }
                        return LifecycleDispatcherServlet.this.environment;
                    }

                    @Override
                    public String getServletContextAttributeName() {
                        // Publish our context as the root web application context. This emulates what
                        // ContextLoaderListener does and is required so that DelegatingFilterProxy and
                        // HttpRequestHandlerServlet instances can find the context later
                        return WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE;
                    }
                };

                LifecycleUtils.updateState(getServletContext(), LifecycleState.STARTING);
                try {
                    // This allows webapps to configure the LifecycleDispatcherServlet as if it was the SpringMVC
                    // DispatcherServlet, accepting all the same parameters, without this class having to manually
                    // copy them all over
                    servlet.init(getServletConfig());

                    delegate = servlet; // If we make it here, SpringMVC has started successfully. Enable the delegate
                    LifecycleUtils.updateState(getServletContext(), LifecycleState.STARTED);
                } catch (final Exception e) {
                    LifecycleUtils.updateState(getServletContext(), LifecycleState.FAILED);
                    log.error("SpringMVC could not be started", e);
                } finally {
                    startup = null; // We're done running, so remove our reference
                }
            }
        });
        thread.setDaemon(true);
        thread.setName("spring-startup");

        startup = thread;
        startup.start();

        if (Boolean.getBoolean(PROP_SYNCHRONOUS)) {
            try {
                thread.join();
            } catch (final InterruptedException e) {
                Thread.currentThread().interrupt();
                log.warn("Interrupted while waiting for synchronous Spring startup to complete");
            }
        }
    }

    @Override
    protected void service(final HttpServletRequest request, final HttpServletResponse response)
            throws ServletException, IOException {
        final DispatcherServlet delegate = this.delegate; // Shadowing intentional
        if (delegate == null) {
            final IEventConfig config = EventAdvisorService.getConfig(getServletContext());

            String nextUrl = request.getRequestURI();

            if (request.getQueryString() != null && !request.getQueryString().isEmpty()) {
                nextUrl += "?" + request.getQueryString();
            }

            final String redirectUrl = request.getContextPath() + config.getErrorPath() + "?next="
                    + URLEncoder.encode(nextUrl, "UTF-8");

            response.sendRedirect(redirectUrl);
        } else {
            delegate.service(request, response);
        }
    }
}
