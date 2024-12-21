package com.pmi.tpd.web.core.rs.container;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.glassfish.hk2.api.Factory;

/**
 * @author Christophe Friederich
 * @since 1.3
 */
public class HttpSessionFactory implements Factory<HttpSession> {

    /** */
    private final HttpServletRequest request;

    @Inject
    public HttpSessionFactory(final HttpServletRequest request) {
        this.request = request;
    }

    @Override
    public HttpSession provide() {
        return request.getSession();
    }

    @Override
    public void dispose(final HttpSession t) {
    }
}
