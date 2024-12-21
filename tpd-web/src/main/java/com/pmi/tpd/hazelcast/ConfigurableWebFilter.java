package com.pmi.tpd.hazelcast;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import com.hazelcast.web.WebFilter;

/**
 * Wrapper around the Hazelcast {@code WebFilter} that manages distribution of session data across the cluster. The
 * wrapper allows enabling/disabling of the session distribution across the cluster by optionally bypassing the
 * Hazelcast filter.
 * 
 * @author Christophe Friederich
 * @since 1.3
 */
public class ConfigurableWebFilter implements Filter {

    /** */
    private final WebFilter delegate;

    /** */
    private final boolean enabled;

    /**
     * @param config
     * @param delegate
     */
    public ConfigurableWebFilter(final HazelcastSessionMode config, final WebFilter delegate) {
        this.enabled = config != HazelcastSessionMode.LOCAL;
        this.delegate = delegate;
    }

    @Override
    public void init(final FilterConfig filterConfig) throws ServletException {
        if (enabled) {
            delegate.init(filterConfig);
        } else {
            // no need to fully initialize the WebFilter, but we do need to register it so SessionListener can find it
            filterConfig.getServletContext().setAttribute(WebFilter.class.getName(), delegate);
        }
    }

    @Override
    public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain chain)
            throws IOException, ServletException {
        if (enabled) {
            delegate.doFilter(request, response, chain);
        } else {
            chain.doFilter(request, response);
        }
    }

    @Override
    public void destroy() {
        if (enabled) {
            delegate.destroy();
        }
    }
}
