package com.pmi.tpd.web.core.servlet;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

/**
 * This filter is used in production, to put HTTP cache headers with a long (1 month) expiration time.
 * 
 * @author Christophe Friederich
 * @since 1.0
 */
public class CachingHttpHeadersFilter implements Filter {

    /** Cache period is 1 month (in ms). */
    private static final long CACHE_PERIOD = TimeUnit.DAYS.toMillis(31L);

    /** We consider the last modified date is the start up time of the server. */
    private static final long LAST_MODIFIED = System.currentTimeMillis();

    @Override
    public void init(final FilterConfig filterConfig) throws ServletException {
        // Nothing to initialise
    }

    @Override
    public void destroy() {
        // Nothing to destroy
    }

    @Override
    public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain chain)
            throws IOException, ServletException {
        final HttpServletResponse httpResponse = (HttpServletResponse) response;

        httpResponse.setHeader("Cache-Control", "max-age=2678400000, public");
        httpResponse.setHeader("Pragma", "cache");

        // Setting Expires header, for proxy caching
        httpResponse.setDateHeader("Expires", CACHE_PERIOD + System.currentTimeMillis());

        // Setting the Last-Modified header, for browser caching
        httpResponse.setDateHeader("Last-Modified", LAST_MODIFIED);

        chain.doFilter(request, response);
    }
}
