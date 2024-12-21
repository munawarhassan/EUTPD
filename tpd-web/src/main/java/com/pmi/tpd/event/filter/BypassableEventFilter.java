package com.pmi.tpd.event.filter;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pmi.tpd.core.event.advisor.servlet.ServletEventFilter;
import com.pmi.tpd.event.EventBypassHelper;

/**
 * Extends the {@link ServletEventFilter} to add {@link EventBypassHelper#isBypassed(ServletRequest) bypass} support.
 *
 * @author Christophe Friederich
 * @since 1.3
 * @see EventBypassHelper
 */
public class BypassableEventFilter extends ServletEventFilter {

    /** */
    private static final Logger LOGGER = LoggerFactory.getLogger(BypassableEventFilter.class);

    @Override
    public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain filterChain)
            throws IOException, ServletException {
        if (EventBypassHelper.isBypassed(request)) {
            // event processing for this filter should be bypassed. Either there are no active events or the correct
            // token was supplied to bypass them.
            if (LOGGER.isTraceEnabled()) {
                LOGGER.trace("Request bypassed: {}", EventBypassHelper.getFullURL((HttpServletRequest) request));
            }
            filterChain.doFilter(request, response);
        } else {
            // If we make it here, either a token was not supplied, the wrong token was supplied, no maintenance is in
            // progress or more events exist than just maintenance (indicating the system is more deeply locked)
            if (LOGGER.isTraceEnabled()) {
                LOGGER.trace("Request NOT bypassed: {}", EventBypassHelper.getFullURL((HttpServletRequest) request));
            }
            super.doFilter(request, response, filterChain);
        }
    }
}
