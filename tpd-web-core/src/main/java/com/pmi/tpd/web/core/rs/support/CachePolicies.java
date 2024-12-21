package com.pmi.tpd.web.core.rs.support;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.Response.ResponseBuilder;

import com.google.common.net.HttpHeaders;

/**
 * DESCRIPTION OF CACHE CONTROL PARAMETERS private ------- Indicates that all or part of the response message is
 * intended for a single user and MUST NOT be cached by a shared cache. This allows an origin server to state that the
 * specified parts of the response are intended for only one user and are not a valid response for requests by other
 * users. A private (non-shared) cache MAY cache the response. no-cache -------- If the no-cache directive does not
 * specify a field-name, then a cache MUST NOT use the response to satisfy a subsequent request without successful
 * re-validation with the origin server. This allows an origin server to prevent caching even by caches that have been
 * configured to return stale responses to client requests. If the no-cache directive does specify one or more
 * field-names, then a cache MAY use the response to satisfy a subsequent request, subject to any other restrictions on
 * caching. However, the specified field-name(s) MUST NOT be sent in the response to a subsequent request without
 * successful re-validation with the origin server. This allows an origin server to prevent the re-use of certain header
 * fields in a response, while still allowing caching of the rest of the response. max-age ------- Indicates that the
 * client is willing to accept a response whose age is no greater than the specified time in seconds. Unless max- stale
 * directive is also included, the client is not willing to accept a stale response.
 */
public class CachePolicies {

    public static void setCacheControl(final ResponseBuilder response, final String value) {
        response.cacheControl(CacheControl.valueOf(value));
    }

    public static void setPragma(final ResponseBuilder response, final String value) {
        response.header(HttpHeaders.PRAGMA, value);
    }

    public static void setExpiration(final ResponseBuilder response, final Date date) {
        response.expires(date);
    }

    public static CacheControl noCache() {
        final CacheControl cacheControl = new CacheControl();
        cacheControl.setNoCache(true);
        return cacheControl;
    }

    public static CacheControl cacheForAMonth() {
        final CacheControl cacheControl = new CacheControl();
        cacheControl.setMaxAge((int) TimeUnit.DAYS.toSeconds(28L));
        cacheControl.setPrivate(true);
        return cacheControl;
    }

    /**
     * @param response
     *            The response to return
     * @param timeSpanInSecond
     *            timeSpanInSecond will be used to calculate the expires value
     */
    private static void cacheFor(final ResponseBuilder response, final long timeSpanInSecond) {
        setExpiration(response, new Date(System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(timeSpanInSecond)));
        setCacheControl(response, "private, max-age=" + timeSpanInSecond);
        setPragma(response, "");
    }

    /**
     * @param response
     *            The response to return
     * @param timeSpan
     *            The time span which works with unit to calculate a real time span
     * @param unit
     *            works with timeSpan
     */
    public static void cacheFor(final ResponseBuilder response, final long timeSpan, final TimeUnit unit) {
        if (TimeUnit.SECONDS.equals(unit)) {
            cacheFor(response, timeSpan);
        } else {
            cacheFor(response, unit.toSeconds(timeSpan));
        }
    }

}
