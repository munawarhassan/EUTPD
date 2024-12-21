package com.pmi.tpd.core.event.advisor.servlet;

import java.io.IOException;
import java.net.URLEncoder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pmi.tpd.api.event.advisor.IEventContainer;

/**
 * A filter that handles cases where the application is unable to handle a normal request and redirects to the
 * configured error path so that a nice error page can be provided.
 *
 * @author Christophe Friederich
 * @since 1.0
 */
public class ServletEventFilter extends AbstractServletEventFilter {

    /** */
    private static final Logger LOGGER = LoggerFactory.getLogger(ServletEventFilter.class);

    @Override
    protected void handleError(final IEventContainer appEventContainer,
        final HttpServletRequest servletRequest,
        final HttpServletResponse servletResponse) throws IOException {
        final String servletPath = getServletPath(servletRequest);
        final String contextPath = servletRequest.getContextPath();
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info(
                "The application is still starting up, or there are errors. Redirecting request from '{}' to '{}'",
                servletPath,
                config.getErrorPath());
        }

        String nextUrl = servletRequest.getRequestURI();

        if (servletRequest.getQueryString() != null && !servletRequest.getQueryString().isEmpty()) {
            nextUrl += "?" + servletRequest.getQueryString();
        }

        final String redirectUrl = contextPath + config.getErrorPath() + "?next=" + URLEncoder.encode(nextUrl, "UTF-8");

        servletResponse.sendRedirect(redirectUrl);
    }

    @Override
    protected void handleNotSetup(final HttpServletRequest servletRequest, final HttpServletResponse servletResponse)
            throws IOException {
        final String servletPath = getServletPath(servletRequest);
        final String contextPath = servletRequest.getContextPath();
        LOGGER.info("The application is not yet setup. Redirecting request from '{}' to '{}'",
            servletPath,
            config.getSetupPath());
        servletResponse.sendRedirect(contextPath + config.getSetupPath());
    }

}
