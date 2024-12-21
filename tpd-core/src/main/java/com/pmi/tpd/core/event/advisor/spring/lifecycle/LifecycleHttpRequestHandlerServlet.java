package com.pmi.tpd.core.event.advisor.spring.lifecycle;

import java.io.IOException;

import javax.annotation.Nonnull;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.pmi.tpd.core.event.advisor.spring.web.context.support.EventHttpRequestHandlerServlet;

/**
 * A specialized {@link EventHttpRequestHandlerServlet} for use with the {@link LifecycleDispatcherServlet}. This
 * servlet automatically redirects to the error page when requests are received during application startup.
 *
 * @author Christophe Friederich
 * @since 1.3
 */
public class LifecycleHttpRequestHandlerServlet extends EventHttpRequestHandlerServlet {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    /** */
    private volatile boolean starting = true;

    @Override
    public void init() throws ServletException {
        final LifecycleState state = LifecycleUtils.getCurrentState(getServletContext());
        if (state == LifecycleState.STARTED) {
            starting = false;

            super.init();
        }
    }

    @Override
    protected void service(@Nonnull final HttpServletRequest request, @Nonnull final HttpServletResponse response)
            throws ServletException, IOException {
        if (starting) {
            if (LifecycleUtils.isStarting(getServletContext())) {
                // Otherwise, if the application is still starting, redirect the request to the error page
                sendRedirect(response);

                return;
            } else {
                // The first time we see a status other than CREATED or STARTING, note that the application
                // is no longer starting and service the request normally
                starting = false;
            }
        }

        super.service(request, response);
    }
}
