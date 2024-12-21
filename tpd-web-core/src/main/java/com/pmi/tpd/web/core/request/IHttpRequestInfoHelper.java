package com.pmi.tpd.web.core.request;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Helper class for providing a {@link IRequestInfoProvider} and other request information based on a given
 * {@code HttpServletRequest}.
 */
public interface IHttpRequestInfoHelper {

    /**
     * Creates a {@link com.pmi.tpd.web.core.request.spi.core.request.spi.IRequestContext} instance based on the request.
     *
     * @param request
     *            the working request
     * @return a {@link com.pmi.tpd.web.core.request.spi.core.request.spi.IRequestContext} instance describing the current request.
     */
    IRequestInfoProvider createRequestInfoProvider(HttpServletRequest request, HttpServletResponse response);

    /**
     * Examines the request for an "X-Forwarded-For" header and, if present, prepends that to the request's
     * {@code getRemoteAddr()} to produce a comma-separated list with addresses for the original client and any proxies
     * which have forwarded the request.
     *
     * @param request
     *            the working request
     * @return a comma-separated list containing 1 or more IP addresses
     */
    String getRemoteAddress(HttpServletRequest request);

    /**
     * Returns the provided request's {@code getRequestURI()} with any context path removed.
     *
     * @param request
     *            the working request
     * @return the request URI without any context path
     */
    String getRequestUrl(HttpServletRequest request);

    /**
     * Uses the CRC32 of a randomly generated id to produce a unique identifier and sets it on the session. Subsequent
     * calls return the value of the attribute rather than regenerating it (which is computationally expensive). If no
     * session is available, {@code null} is returned. A session is not created for the request if one does not already
     * exist.
     *
     * @param request
     *            the working request
     * @return a consistent unique identifier for the session, if any, associated with the provided request
     */
    String getSessionId(HttpServletRequest request);
}
