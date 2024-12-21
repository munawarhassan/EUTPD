package com.pmi.tpd.core.event.advisor.servlet;

import java.io.IOException;
import java.util.Collection;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;

import com.pmi.tpd.api.event.advisor.IEventContainer;
import com.pmi.tpd.api.event.advisor.event.Event;
import com.pmi.tpd.core.event.advisor.IRequestEventCheck;
import com.pmi.tpd.core.event.advisor.config.IEventConfig;
import com.pmi.tpd.core.event.advisor.setup.ISetupConfig;

/**
 * Base class for handling error cases where the application is unavailable to handle normal requests.
 *
 * @author Christophe Friederich
 * @since 1.3
 */
public abstract class AbstractServletEventFilter implements Filter {

    /** */
    protected static final String TEXT_XML_UTF8_CONTENT_TYPE = "text/xml;charset=utf-8";

    /** */
    protected FilterConfig filterConfig;

    /** */
    protected IEventConfig config;

    /**
     * This filter checks to see if there are any application consistency errors before any pages are accessed. If there
     * are errors then a redirect to the errors page is made
     */
    @Override
    public void doFilter(final ServletRequest servletRequest,
        final ServletResponse servletResponse,
        final FilterChain filterChain) throws IOException, ServletException {
        final String alreadyFilteredKey = getClass().getName() + "_already_filtered";
        if (servletRequest.getAttribute(alreadyFilteredKey) != null) {
            filterChain.doFilter(servletRequest, servletResponse);
            return;
        } else {
            servletRequest.setAttribute(alreadyFilteredKey, Boolean.TRUE);
        }

        final HttpServletRequest request = (HttpServletRequest) servletRequest;
        final HttpServletResponse response = (HttpServletResponse) servletResponse;

        // get the URI of this request
        final String servletPath = getServletPath(request);

        // Get the container for this context and run all of the configured request event checks
        final IEventContainer appEventContainer = getContainerAndRunEventChecks(request);

        final ISetupConfig setup = config.getSetupConfig();

        // if there are application consistency events then redirect to the errors page
        final boolean ignoreUri = ignoreURI(servletPath);
        if (appEventContainer.hasEvents() && !ignoreUri) {
            handleError(appEventContainer, request, response);
        } else if (!ignoreUri && !setup.isSetup() && !setup.isSetupPage(servletPath)) {
            // if application is not setup then send to the Setup Page
            handleNotSetup(request, response);
        } else {
            filterChain.doFilter(servletRequest, servletResponse);
        }
    }

    @Override
    public void destroy() {
    }

    @Override
    public void init(final FilterConfig filterConfig) {
        this.filterConfig = filterConfig;

        config = ServletEventAdvisor.getInstance().getConfig();
    }

    protected IEventContainer getContainerAndRunEventChecks(final HttpServletRequest req) {
        // Get the container for this context
        final IEventContainer appEventContainer = ServletEventAdvisor.getInstance()
                .getEventContainer(filterConfig.getServletContext());
        // run all of the configured request event checks
        for (final IRequestEventCheck<HttpServletRequest> requestEventCheck : config
                .<HttpServletRequest> getRequestEventChecks()) {
            requestEventCheck.check(ServletEventAdvisor.getInstance(), req);
        }
        return appEventContainer;
    }

    /**
     * Retrieves the current request servlet path. Deals with differences between servlet specs (2.2 vs 2.3+)
     * <p/>
     * Taken from the Webwork RequestUtils class
     *
     * @param request
     *            the request
     * @return the servlet path
     */
    protected static String getServletPath(final HttpServletRequest request) {
        final String servletPath = request.getServletPath();
        if (StringUtils.isNotEmpty(servletPath)) {
            return servletPath;
        }

        final String requestUri = request.getRequestURI();
        final int startIndex = request.getContextPath().equals("") ? 0 : request.getContextPath().length();
        int endIndex = request.getPathInfo() == null ? requestUri.length()
                : requestUri.lastIndexOf(request.getPathInfo());
        if (startIndex > endIndex) {
            // this should not happen
            endIndex = startIndex;
        }

        return requestUri.substring(startIndex, endIndex);
    }

    protected String getStringForEvents(final Collection<Event> events) {
        final StringBuilder message = new StringBuilder();
        for (final Event event : events) {
            if (message.length() > 0) {
                message.append("\n");
            }
            message.append(event.getDesc());
        }
        return message.toString();
    }

    /**
     * Handles the given request for error cases when there is a {@link Event} which stops normal application
     * functioning.
     *
     * @param appEventContainer
     *            the {@link IEventContainer} that contains the events.
     * @param servletRequest
     *            the request being directed to the error.
     * @param servletResponse
     *            the response.
     * @throws IOException
     *             when the error cannot be handled.
     */
    protected abstract void handleError(IEventContainer appEventContainer,
        HttpServletRequest servletRequest,
        HttpServletResponse servletResponse) throws IOException;

    /**
     * Handles the given request for cases when the application is not yet setup which stops normal application
     * functioning.
     *
     * @param servletRequest
     *            the request being directed to the error.
     * @param servletResponse
     *            the response.
     * @throws IOException
     *             when the error cannot be handled.
     */
    protected abstract void handleNotSetup(HttpServletRequest servletRequest, HttpServletResponse servletResponse)
            throws IOException;

    /**
     * @param uri
     * @return
     */
    protected boolean ignoreURI(final String uri) {
        return uri.equalsIgnoreCase(config.getErrorPath()) || config.isIgnoredPath(uri);
    }
}
