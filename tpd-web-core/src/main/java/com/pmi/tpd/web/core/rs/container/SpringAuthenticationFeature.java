package com.pmi.tpd.web.core.rs.container;

import java.net.URI;

import javax.ws.rs.core.Feature;
import javax.ws.rs.core.FeatureContext;

import com.pmi.tpd.web.core.rs.container.internal.SpringAuthenticationFilter;

public final class SpringAuthenticationFeature implements Feature {

    /** */
    private String username;

    /** */
    private String password;

    /** */
    private boolean csrf = false;

    /** */
    private URI uri;

    /** */
    private String loginPath;

    private SpringAuthenticationFeature() {
    }

    public static SpringAuthenticationFeature createCsrfAuthentication(final URI uri,
        final String loginPath,
        final String username,
        final String password) {
        final SpringAuthenticationFeature feature = new SpringAuthenticationFeature();
        feature.username = username;
        feature.password = password;
        feature.loginPath = loginPath;
        feature.uri = uri;
        feature.csrf = true;
        return feature;
    }

    public static SpringAuthenticationFeature createAuthentication(final URI uri,
        final String loginPath,
        final String username,
        final String password) {
        final SpringAuthenticationFeature feature = new SpringAuthenticationFeature();
        feature.username = username;
        feature.password = password;
        feature.loginPath = loginPath;
        feature.uri = uri;
        return feature;
    }

    @Override
    public boolean configure(final FeatureContext context) {
        context.register(new SpringAuthenticationFilter(uri, loginPath, username, password, csrf));
        return true;
    }

}
