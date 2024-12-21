package com.pmi.tpd.startup;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.AsyncHandlerInterceptor;
import org.springframework.web.servlet.HandlerMapping;

import com.pmi.tpd.api.context.IApplicationProperties;
import com.pmi.tpd.web.rest.rsrc.api.setup.SetupResource;

/**
 * If the application was not setup, redirect all the requests received by the MVC framework to <tt>/setup</tt>.
 *
 * @author Christophe Friederich
 * @since 1.3
 */
public final class SetupRedirectInterceptor implements AsyncHandlerInterceptor {

    /** */
    private static final String SETUP_PATH = "/" + SetupResource.FULL_PAGE_NAME;

    /** */
    // package private for tests
    static final String SETUP_ATTRIBUTE = "preHandledBy-" + SetupRedirectInterceptor.class.getSimpleName();

    private final IApplicationProperties settings;

    public SetupRedirectInterceptor(final IApplicationProperties settings) {
        this.settings = settings;
    }

    @Override
    public boolean preHandle(final HttpServletRequest request, final HttpServletResponse response, final Object handler)
            throws Exception {
        if (settings.isSetup()) {
            return true;
        }

        if (shouldRedirect(request)) {
            response.sendRedirect(request.getContextPath() + SETUP_PATH);
            return false;
        }

        // we set this attribute to ensure that requests forwarded to error pages by the SetupController are not
        // redirected to the setup page (causing a redirect loop for non-transient errors)
        request.setAttribute(SETUP_ATTRIBUTE, Boolean.TRUE);

        return true;
    }

    private boolean shouldRedirect(final HttpServletRequest request) {
        return !SETUP_PATH.equalsIgnoreCase(getRequestPath(request))
                && !Boolean.TRUE.equals(request.getAttribute(SETUP_ATTRIBUTE));
        // && !XsrfTokenInterceptor.isForwarded(request);
    }

    private String getRequestPath(final HttpServletRequest request) {
        return (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
    }

}
