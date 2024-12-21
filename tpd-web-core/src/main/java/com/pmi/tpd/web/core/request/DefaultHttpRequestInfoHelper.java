package com.pmi.tpd.web.core.request;

import java.security.SecureRandom;
import java.util.zip.CRC32;

import javax.annotation.Nonnull;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pmi.tpd.api.Product;
import com.pmi.tpd.security.random.SecureRandomFactory;

/**
 * A default implementation of the {@link IHttpRequestInfoHelper}.
 *
 * @author Christophe Friederich
 * @since 1.3
 */
public class DefaultHttpRequestInfoHelper implements IHttpRequestInfoHelper {

    /** */
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultHttpRequestInfoHelper.class);

    /**
     * Attribute set on a {@code HttpSession} containing the unique identifier generated for the session by
     * {@link #getSessionId(javax.servlet.http.HttpServletRequest)}, allowing the same ID to be returned repeatably
     * without the computational expense of regenerating it each time.
     */
    static final String ATTR_SESSION_ID = Product.getPrefix() + ".session-id";

    /** */
    private final SecureRandom secureRandom;

    /**
     * Constructs a new {@code DefaultHttpRequestInfoHelper} and initialises the {@code SecureRandom} generator which
     * will be used to create new session IDs.
     */
    public DefaultHttpRequestInfoHelper() {
        this.secureRandom = SecureRandomFactory.newInstance();
    }

    @Override
    public IRequestInfoProvider createRequestInfoProvider(final HttpServletRequest request,
        final HttpServletResponse response) {
        return new HttpRequestInfoProvider(request, response);
    }

    /**
     * Examines the request for an "X-Forwarded-For" header and, if present, prepends that to the request's
     * {@code getRemoteAddr()} to produce a comma-separated list with addresses for the original client and any proxies
     * which have forwarded the request.
     *
     * @param request
     *                the working request
     * @return a comma-separated list containing 1 or more IP addresses
     */
    @Override
    public String getRemoteAddress(final HttpServletRequest request) {
        final StringBuilder builder = new StringBuilder();

        final String header = request.getHeader("X-Forwarded-For");
        if (header != null) {
            // In the presence of X-Forwarded-For, the remote address is the final proxy in the chain, so we need both
            // the value of the header _and_ the remote address.
            builder.append(header).append(",");
        }
        builder.append(request.getRemoteAddr());

        // Remove all whitespace from the address string to make the log file more parseable
        return builder.toString().replaceAll("\\s", "");
    }

    /**
     * Returns the provided request's {@code getRequestURI()} with any context path removed.
     *
     * @param request
     *                the working request
     * @return the request URI without any context path
     */
    @Override
    public String getRequestUrl(final HttpServletRequest request) {
        String url = request.getRequestURI();
        if (StringUtils.isNotBlank(request.getContextPath())) {
            url = url.substring(request.getContextPath().length());
        }
        return url;
    }

    /**
     * Uses the CRC32 of a randomly generated session ID to produce a unique identifier and sets it under
     * {@link #ATTR_SESSION_ID} on the session. Subsequent calls return the value of the attribute rather than
     * regenerating it (which is computationally expensive). If no session is available, {@code null} is returned.
     *
     * @param request
     *                the working request
     * @return a consistent unique identifier for the session, if any, associated with the provided request
     */
    @Override
    public String getSessionId(final HttpServletRequest request) {
        final HttpSession session = request.getSession(false);
        if (session == null) {
            return null;
        }

        String sessionId = null;
        try {
            sessionId = (String) session.getAttribute(ATTR_SESSION_ID);
            if (sessionId == null) {
                final byte[] bytes = new byte[40];
                secureRandom.nextBytes(bytes);

                final CRC32 crc = new CRC32();
                crc.update(bytes);

                sessionId = Long.toString(crc.getValue(), Character.MAX_RADIX);
                session.setAttribute(ATTR_SESSION_ID, sessionId);
            }
        } catch (final IllegalStateException e) {
            // the session has been invalidated; we cannot access the attributes
            LOGGER.debug("Could not retrieve sessionId: {0}", StringUtils.defaultString(e.getMessage()));
        }
        return sessionId;
    }

    /**
     * @author Christophe Friederich
     * @since 1.3
     */
    private final class HttpRequestInfoProvider implements IRequestInfoProvider {

        /** */
        private final HttpServletRequest request;

        /** */
        private final HttpServletResponse response;

        /** */
        private String action;

        /** */
        private String requestDetails;

        /** */
        private String sessionId;

        private HttpRequestInfoProvider(final HttpServletRequest request, final HttpServletResponse response) {
            this.request = request;
            this.response = response;
        }

        @Nonnull
        @Override
        public String getAction() {
            if (action == null) {
                action = "\"" + request.getMethod() + " " + getRequestUrl(request) + " " + request.getProtocol() + "\"";
            }
            return action;
        }

        @Override
        public String getDetails() {
            if (requestDetails == null) {
                requestDetails = String.format("\"%1$s\" \"%2$s\"",
                    StringUtils.defaultString(StringUtils.substringBefore(request.getHeader("referer"), "?")),
                    StringUtils.defaultString(request.getHeader("user-agent")));
            }
            return requestDetails;
        }

        @Nonnull
        @Override
        public String getProtocol() {
            return request.getScheme();
        }

        @Nonnull
        @Override
        public Object getRawRequest() {
            return request;
        }

        @Nonnull
        @Override
        public Object getRawResponse() {
            return response;
        }

        @Override
        public String getRemoteAddress() {
            return DefaultHttpRequestInfoHelper.this.getRemoteAddress(request);
        }

        @Override
        public String getSessionId() {
            if (sessionId == null) {
                sessionId = DefaultHttpRequestInfoHelper.this.getSessionId(request);
            }
            return sessionId;
        }

        @Override
        public boolean hasSessionId() {
            return getSessionId() != null;
        }

        @Override
        public boolean isSecure() {
            return request.isSecure();
        }
    }
}
