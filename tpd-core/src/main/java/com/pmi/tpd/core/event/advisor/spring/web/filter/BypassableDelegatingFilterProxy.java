package com.pmi.tpd.core.event.advisor.spring.web.filter;

import java.io.IOException;

import javax.annotation.Nonnull;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.DelegatingFilterProxy;

/**
 * A more permissive subclass of Spring's {@code DelegatingFilterProxy} which bypasses the filter if there is no Spring
 * {@code WebApplicationContext} available.
 * <p/>
 * The default behavior of the base class is to throw an {@code IllegalStateException} if, by the time the filter is
 * invoked for the first time, no context is available. This subclass bypasses that behavior and invokes the filter
 * chain instead. This is necessary to allow redirects to the configured error page to make it through a filter chain
 * which includes delegating filters.
 * <p/>
 * Note: This class preserves the behavior from the base class where the proxy will fail if it cannot find a bean with
 * the correct name. It only suppresses failing if Spring is completely unavailable.
 *
 * @author Christophe Friederich
 * @since 1.3
 */
public class BypassableDelegatingFilterProxy extends DelegatingFilterProxy {

    /** */
    private static final Logger LOGGER = LoggerFactory.getLogger(BypassableDelegatingFilterProxy.class);

    /** */
    private volatile boolean bypassable = true;

    /**
     * Bypasses execution of the filter if no {@code WebApplicationContext} is available, delegating directly to the
     * filter chain, or performs normal filtering if a context is available.
     * <p/>
     * Once a {@code WebApplicationContext} is available once, this filter will never bypass again (even if, for some
     * reason, the context later disappears).
     *
     * @param request
     *            the servlet request to filter
     * @param response
     *            the servlet response
     * @param filterChain
     *            the filter chain
     * @throws ServletException
     *             May be thrown by the base class or the chain; never thrown locally.
     * @throws IOException
     *             May be thrown by the base class or the chain; never thrown locally.
     */
    @Override
    public void doFilter(@Nonnull final ServletRequest request,
        @Nonnull final ServletResponse response,
        @Nonnull final FilterChain filterChain) throws ServletException, IOException {
        if (bypassable && findWebApplicationContext() == null) {
            LOGGER.warn("Bypassing [{}]; no Spring WebApplicationContext is available", getFilterName());
            filterChain.doFilter(request, response);
        } else {
            LOGGER.trace("Found Spring WebApplicationContext; attempting to invoke delegate");
            super.doFilter(request, response, filterChain);
        }
    }

    /**
     * As an optimisation to this approach, once the delegate is initialised we set a flag indicating the filter is no
     * longer bypassable. That way we don't continue to resolve the {@code WebApplicationContext} unnecessarily on every
     * request.
     *
     * @param wac
     *            the resolved {@code WebApplicationContext}
     * @return the targeted filter
     * @throws ServletException
     *             See documentation for the base class.
     * @see DelegatingFilterProxy#initDelegate(org.springframework.web.context.WebApplicationContext)
     */
    @Override
    protected Filter initDelegate(final WebApplicationContext wac) throws ServletException {
        LOGGER.debug("Filter [{}] is no longer bypassable; the WebApplicationContext is available", getFilterName());
        bypassable = false;

        return super.initDelegate(wac);
    }
}
