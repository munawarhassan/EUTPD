package com.pmi.tpd.web.core.servlet;

import java.io.IOException;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.filter.OncePerRequestFilter;

import com.pmi.tpd.web.core.request.IHttpRequestInfoHelper;
import com.pmi.tpd.web.core.request.IRequestManager;
import com.pmi.tpd.web.core.request.spi.IRequestContext;

/**
 * Adds attributes about the current {@code HttpServletRequest} to the logging MDC and as response headers. Be careful
 * when moving this filter in the filter chain, because this operation needs to happen as close to the boundaries of the
 * request as possible.
 * <p>
 * Note: This consolidates MDC and headers in a single filter purely to try and reduce the overall depth of the filter
 * chain. Given that the values and concepts are all the same, just with different targets, it seems wasteful to use
 * multiple filters to do the work.
 * </p>
 *
 * @author Christophe Friederich
 * @since 1.0
 */

public class RequestAttributeFilter extends OncePerRequestFilter {

    /** */
    public static final String X_AREQUESTID = "X-AREQUESTID";

    /** */
    public static final String X_ASESSIONID = "X-ASESSIONID";

    /** */
    private final IHttpRequestInfoHelper helper;

    /** */
    private final IRequestManager requestManager;

    /**
     * Default constructor.
     *
     * @param helper
     *                       helper providing other request information based on a given {@code HttpServletRequest}.
     * @param requestManager
     *                       the manager http request.
     */
    @Inject
    public RequestAttributeFilter(final IHttpRequestInfoHelper helper, final IRequestManager requestManager) {
        this.helper = helper;
        this.requestManager = requestManager;
    }

    @Override
    protected void doFilterInternal(final HttpServletRequest request,
        final HttpServletResponse response,
        final FilterChain filterChain) throws ServletException, IOException {
        try {
            requestManager.doAsRequest((@Nonnull final IRequestContext requestContext) -> {
                // Set response headers for the request ID, and the session ID if one is available
                response.setHeader(X_AREQUESTID, requestContext.getId());
                if (requestContext.hasSessionId()) {
                    response.setHeader(X_ASESSIONID, requestContext.getSessionId());
                }

                filterChain.doFilter(request, response);

                return null;
            }, helper.createRequestInfoProvider(request, response));
        } catch (final Exception e) {
            if (e instanceof ServletException) {
                throw (ServletException) e;
            }
            if (e instanceof IOException) {
                throw (IOException) e;
            }
            throw (RuntimeException) e;
        }
    }

    /**
     * In order to render /static/error404 and /static/error500 in an error dispatch
     * {@link IRequestManager.request.RequestManager#getRequestContext()} must not be null. Therefore we must ensure
     * error dispatches are filtered
     */
    @Override
    protected boolean shouldNotFilterErrorDispatch() {
        return false;
    }
}
