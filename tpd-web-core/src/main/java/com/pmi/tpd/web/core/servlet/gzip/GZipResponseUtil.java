package com.pmi.tpd.web.core.servlet.gzip;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Christophe Friederich
 * @since 1.0
 */
public final class GZipResponseUtil {

    /** logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(GZipResponseUtil.class);

    /**
     * Gzipping an empty file or stream always results in a 20 byte output This is in java or elsewhere.
     * <p/>
     * On a unix system to reproduce do <code>gzip -n empty_file</code>. -n tells gzip to not include the file name. The
     * resulting file size is 20 bytes.
     * <p/>
     * Therefore 20 bytes can be used indicate that the gzip byte[] will be empty when ungzipped.
     */
    private static final int EMPTY_GZIPPED_CONTENT_SIZE = 20;

    /**
     * Utility class. No public constructor.
     */
    private GZipResponseUtil() {
    }

    /**
     * Checks whether a gzipped body is actually empty and should just be zero. When the compressedBytes is
     * {@link #EMPTY_GZIPPED_CONTENT_SIZE} it should be zero.
     *
     * @param compressedBytes
     *            the gzipped response body
     * @param request
     *            the client HTTP request
     * @return true if the response should be 0, even if it is isn't.
     */
    public static boolean shouldGzippedBodyBeZero(final byte[] compressedBytes, final HttpServletRequest request) {

        // Check for 0 length body
        if (compressedBytes.length == EMPTY_GZIPPED_CONTENT_SIZE) {
            if (LOGGER.isTraceEnabled()) {
                LOGGER.trace("{} resulted in an empty response.", request.getRequestURL());
            }
            return true;
        } else {
            return false;
        }
    }

    /**
     * Performs a number of checks to ensure response saneness according to the rules of RFC2616:
     * <ol>
     * <li>If the response code is {@link javax.servlet.http.HttpServletResponse#SC_NO_CONTENT} then it is illegal for
     * the body to contain anything. See http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.2.5
     * <li>If the response code is {@link javax.servlet.http.HttpServletResponse#SC_NOT_MODIFIED} then it is illegal for
     * the body to contain anything. See http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.3.5
     * </ol>
     *
     * @param request
     *            the client HTTP request
     * @param responseStatus
     *            the responseStatus
     * @return true if the response should be 0, even if it is isn't.
     */
    public static boolean shouldBodyBeZero(final HttpServletRequest request, final int responseStatus) {

        // Check for NO_CONTENT
        if (responseStatus == HttpServletResponse.SC_NO_CONTENT) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("{} resulted in a {} response. Removing message body in accordance with RFC2616.",
                    request.getRequestURL(),
                    HttpServletResponse.SC_NO_CONTENT);
            }
            return true;
        }

        // Check for NOT_MODIFIED
        if (responseStatus == HttpServletResponse.SC_NOT_MODIFIED) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("{} resulted in a {} response. Removing message body in accordance with RFC2616.",
                    request.getRequestURL(),
                    HttpServletResponse.SC_NOT_MODIFIED);
            }
            return true;
        }
        return false;
    }

    /**
     * Adds the gzip HTTP header to the response.
     * <p/>
     * <p>
     * This is need when a gzipped body is returned so that browsers can properly decompress it.
     * </p>
     *
     * @param response
     *            the response which will have a header added to it. I.e this method changes its parameter
     * @throws GzipResponseHeadersNotModifiableException
     *             Either the response is committed or we were called using the include method from a
     *             RequestDispatcher#include(ServletRequest, ServletResponse) method and the set header is ignored.
     * @see javax.servlet.RequestDispatcher#include(javax.servlet.ServletRequest, javax.servlet.ServletResponse)
     */
    public static void addGzipHeader(final HttpServletResponse response)
            throws GzipResponseHeadersNotModifiableException {
        response.setHeader("Content-Encoding", "gzip");
        final boolean containsEncoding = response.containsHeader("Content-Encoding");
        if (!containsEncoding) {
            throw new GzipResponseHeadersNotModifiableException(
                    "Failure when attempting to set " + "Content-Encoding: gzip");
        }
    }
}
