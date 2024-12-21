package com.pmi.tpd.web.core.rs.container.internal;

import java.io.IOException;
import java.net.URI;
import java.util.Collection;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.lang3.StringUtils;
import org.glassfish.jersey.client.JerseyClientBuilder;

public class SpringAuthenticationFilter implements ClientRequestFilter {

    private static final String X_CSRF_TOKEN = "X-CSRF-TOKEN";

    private static final String CSRF_TOKEN = "CSRF-TOKEN";

    private final String username;

    private final String password;

    private final URI uri;

    private final String loginPath;

    private final boolean csrf;

    private Collection<? extends Cookie> cookies;

    private final Client client;

    /**
     *
     */
    public SpringAuthenticationFilter(final URI uri, final String loginPath, final String username,
            final String password, final boolean csrf) {
        this.username = username;
        this.password = password;
        this.uri = uri;
        this.loginPath = loginPath;
        this.csrf = csrf;
        this.client = JerseyClientBuilder.createClient();
    }

    @Override
    public void filter(final ClientRequestContext requestContext) throws IOException {
        String csrfToken = null;
        if (csrf) {
            csrfToken = negotiateToken(requestContext);
        }
        if (StringUtils.isNotEmpty(this.username)) {
            submitForm(requestContext, csrfToken);
        }
    }

    public String negotiateToken(final ClientRequestContext requestContext) {
        final WebTarget target = client.target(uri);
        final Response response = target.path("rest/api/secure/token").request().get();
        String csrfToken = response.getHeaderString(X_CSRF_TOKEN);
        if (csrfToken == null) {
            final NewCookie cookie = response.getCookies().get(CSRF_TOKEN);
            if (cookie != null) {
                csrfToken = cookie.getValue();
            }
        }
        if (csrfToken == null) {
            requestContext.abortWith(Response.status(Status.FORBIDDEN).build());

        } else {
            cookies = response.getCookies().values();
        }
        return csrfToken;
    }

    public void submitForm(final ClientRequestContext requestContext, final String csrfToken) {
        final WebTarget target = client.target(uri);
        final Form form = new Form();
        form.param("j_username", username)
                .param("j_password", password)
                .param("_spring_security_remember_me", "false")
                .param("submit", "Login");
        final Invocation.Builder builder = target.path(loginPath).request(MediaType.APPLICATION_FORM_URLENCODED);
        if (csrfToken != null) {
            builder.header(X_CSRF_TOKEN, csrfToken);
        }
        if (cookies != null) {
            for (final Cookie cookie : cookies) {
                builder.cookie(cookie);
            }
        }
        final Response response = builder.post(Entity.form(form));
        if (response.getStatus() != Status.OK.getStatusCode()) {
            requestContext.abortWith(response);
        }
        cookies = response.getCookies().values();
        if (cookies != null) {
            for (final Cookie cookie : cookies) {
                requestContext.getHeaders()
                        .add(HttpHeaders.COOKIE,
                            new Cookie(cookie.getName(), cookie.getValue(), cookie.getPath(), cookie.getPath(),
                                    cookie.getVersion()));
            }
        }
        if (csrfToken != null) {
            requestContext.getHeaders().add(X_CSRF_TOKEN, csrfToken);
        }
        requestContext.getHeaders().add(HttpHeaders.COOKIE, new Cookie(CSRF_TOKEN, csrfToken));
    }

}
