package com.pmi.tpd.event.filter;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pmi.tpd.api.event.advisor.IEventContainer;
import com.pmi.tpd.core.event.advisor.servlet.Servlet503EventFilter;
import com.pmi.tpd.core.event.advisor.spring.lifecycle.LifecycleState;
import com.pmi.tpd.core.event.advisor.spring.lifecycle.LifecycleUtils;
import com.pmi.tpd.event.EventBypassHelper;

/**
 * Extends the {@link Servlet503EventFilter} to add {@link EventBypassHelper#isBypassed(ServletRequest) bypass} support.
 *
 * @author Christophe Friederich
 * @since 1.3
 * @see EventBypassHelper
 */
public class BypassableEvent503Filter extends Servlet503EventFilter {

    /** */
    private static final Logger LOGGER = LoggerFactory.getLogger(BypassableEvent503Filter.class);

    @Override
    public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain filterChain)
            throws IOException, ServletException {
        if (EventBypassHelper.isBypassed(request)) {
            // Event processing for this filter should be bypassed. Either there are no active events or the correct
            // token was supplied to bypass them.
            if (LOGGER.isTraceEnabled()) {
                LOGGER.trace("Request 503 bypassed: {}", EventBypassHelper.getFullURL((HttpServletRequest) request));
            }
            final HttpServletRequest httpRequest = (HttpServletRequest) request;
            final HttpServletResponse httpResponse = (HttpServletResponse) response;
            final ServletContext servletContext = httpRequest.getServletContext();
            if (!isStarted(servletContext) && !EventBypassHelper.isIgnored(httpRequest, servletContext)) {
                final IEventContainer appEventContainer = getContainerAndRunEventChecks(httpRequest);
                this.handleError(appEventContainer, httpRequest, httpResponse);
            } else {
                filterChain.doFilter(request, response);
            }
        } else {
            // If we make it here, either a token was not supplied, the wrong token was supplied, no maintenance is in
            // progress or more events exist than just maintenance (indicating the system is more deeply locked)
            if (LOGGER.isTraceEnabled()) {
                LOGGER.trace("Request 503 NOT bypassed: {}",
                    EventBypassHelper.getFullURL((HttpServletRequest) request));
            }
            super.doFilter(request, response, filterChain);
        }
    }

    private static boolean isStarted(final ServletContext servletContext) {
        final LifecycleState state = LifecycleUtils.getCurrentState(servletContext);
        return LifecycleState.STARTED.equals(state);
    }
}
