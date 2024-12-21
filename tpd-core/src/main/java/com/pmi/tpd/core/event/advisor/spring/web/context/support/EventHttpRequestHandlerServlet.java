package com.pmi.tpd.core.event.advisor.spring.web.context.support;

import java.io.IOException;

import javax.annotation.Nonnull;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.support.HttpRequestHandlerServlet;

import com.pmi.tpd.api.event.advisor.event.Event;
import com.pmi.tpd.core.event.advisor.EventAdvisorService;
import com.pmi.tpd.core.event.advisor.config.IEventConfig;
import com.pmi.tpd.core.event.advisor.spring.web.context.EventContextLoaderListener;

/**
 * @author Christophe Friederich
 * @since 1.3
 */
public class EventHttpRequestHandlerServlet extends HttpRequestHandlerServlet {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    /** */
    private static final Logger LOGGER = LoggerFactory.getLogger(EventHttpRequestHandlerServlet.class);

    /** */
    private final Object lock = new Object();

    /** */
    private volatile boolean uninitialised = true;

    @Override
    public void init() throws ServletException {
        // During startup, we'd like to initialise if possible because doing so here doesn't require locking. However,
        // if the WebApplicationContext was bypassed, attempting to initialise here will fail and may make the entire
        // server inaccessible. That would prevent accessing the event page, so if the WebApplicationContext is not
        // available we bypass initialisation here.
        maybeInit();
    }

    protected void sendRedirect(@Nonnull final HttpServletResponse response) throws IOException {
        final ServletContext servletContext = getServletContext();
        final IEventConfig config = EventAdvisorService.getConfig(servletContext);

        LOGGER.warn("HttpRequestHandlerServlet [{}] cannot be initialised to service an incoming "
                + "request; redirecting to {}",
            getServletName(), config.getErrorPath());
        response.sendRedirect(servletContext.getContextPath() + config.getErrorPath());
    }

    @Override
    protected void service(@Nonnull final HttpServletRequest request, @Nonnull final HttpServletResponse response)
            throws ServletException, IOException {
        // When a request comes in, we're required to be initialised. If we're not, the only possible reason is that
        // the application is evented. That suggests we will not be able to initialise here, but we still try, on
        // the assumption that this URL was allowed through the Event filter and may, therefore, work. If so, the
        // request will be processed normally.
        if (uninitialised) {
            synchronized (lock) {
                if (uninitialised) {
                    if (!maybeInit()) {
                        // If we can't initialise at this point, redirect to the Event error page. The filter should
                        // not have allowed access to this URL.
                        sendRedirect(response);

                        return;
                    }
                }
            }
        }

        super.service(request, response);
    }

    private boolean maybeInit() throws ServletException {
        final ServletContext servletContext = getServletContext();

        final Object attribute = servletContext.getAttribute(EventContextLoaderListener.ATTR_BYPASSED);
        // First, check to see if the WebApplicationContext was bypassed. If it was, it's possible, based on
        // configuration, that no event was added. However, we must bypass handler initialisation as well,
        // because no parent context will be available.
        if (Boolean.TRUE == attribute) {
            // Fully bypassed, without even trying to start
            LOGGER.error("Bypassing HttpRequestHandlerServlet [{}] initialisation; Spring initialisation was bypassed",
                getServletName());
            return false;
        }
        // If WebApplicationContext initialisation wasn't bypassed, check to see if it failed. Handler initialisation
        // is guaranteed to fail if the primary WebApplicationContext failed, so we'll want to bypass it.
        if (attribute instanceof Event) {
            final Event event = (Event) attribute;

            LOGGER.error("Bypassing HttpRequestHandlerServlet [{}] initialisation; Spring initialisation failed: {}",
                getServletName(),
                event.getDesc());
            return false;
        }

        // If we make it here, the Spring WebApplicationContext should have started successfully. That means it's safe
        // to try and start this handler.
        try {
            super.init();

            // No longer uninitialised, so future calls through this servlet should be serviced normally. Setting this
            // optimises the locking out of the service(...) method.
            uninitialised = false;
        } catch (final Exception e) {
            LOGGER.error("HttpRequestHandlerServlet [" + getServletName() + "] could not be started", e);

            return false;
        }

        return true;
    }
}
