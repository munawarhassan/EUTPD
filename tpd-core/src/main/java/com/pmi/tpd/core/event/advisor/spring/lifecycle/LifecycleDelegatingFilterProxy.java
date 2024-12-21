package com.pmi.tpd.core.event.advisor.spring.lifecycle;

import java.io.IOException;

import javax.annotation.Nonnull;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import com.pmi.tpd.core.event.advisor.spring.web.filter.BypassableDelegatingFilterProxy;

/**
 * A specialized {@link BypassableDelegatingFilterProxy} for use with the {@link LifecycleDispatcherServlet}. This proxy
 * automatically bypasses the filter when requests are received during application startup.
 *
 * @author Christophe Friederich
 * @since 1.3
 */
public class LifecycleDelegatingFilterProxy extends BypassableDelegatingFilterProxy {

    /** */
    private volatile boolean starting = true;

    @Override
    public void doFilter(@Nonnull final ServletRequest request,
        @Nonnull final ServletResponse response,
        @Nonnull final FilterChain filterChain) throws ServletException, IOException {
        if (starting) {
            if (LifecycleUtils.isStarting(getServletContext())) {
                // Otherwise, if the application is still starting, bypass the filter and invoke the chain
                filterChain.doFilter(request, response);

                return;
            } else {
                // The first time we see a status other than CREATED or STARTING, note that the application
                // is no longer starting and apply the filter normally
                starting = false;
            }
        }

        super.doFilter(request, response, filterChain);
    }
}
