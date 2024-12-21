package com.pmi.tpd.core.security.web.auth;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import com.pmi.tpd.core.security.util.ConstantTimeComparison;
import com.pmi.tpd.security.IAuthenticationContext;
import com.pmi.tpd.security.random.ISecureTokenGenerator;

/**
 * Initially inspired by xwork, just with the session key changed in case the xwork implementation makes incompatible
 * changes in the future. The request parameter name is kept the same as you won't have mixed XWork and SpringMVC forms
 * competing over setting it. (we hope)
 */
public class HttpSessionXsrfTokenGenerator implements XsrfTokenGenerator {

    static final String TOKEN_SESSION_KEY = "app.xsrf.token";

    static final String REQUEST_PARAM_NAME = "atl_token";

    private final ISecureTokenGenerator secureTokenGenerator;

    private final IAuthenticationContext authenticationContext;

    @Inject
    public HttpSessionXsrfTokenGenerator(final ISecureTokenGenerator secureTokenGenerator,
            final IAuthenticationContext authenticationContext) {
        this.secureTokenGenerator = secureTokenGenerator;
        this.authenticationContext = authenticationContext;
    }

    @Override
    public String generateToken(final HttpServletRequest request) {
        final HttpSession session = getSession(request);
        if (session == null) {
            return null;
        }

        String token = (String) session.getAttribute(TOKEN_SESSION_KEY);

        if (token == null) {
            token = createToken();
            session.setAttribute(TOKEN_SESSION_KEY, token);
        }

        return token;
    }

    @Override
    public String getXsrfTokenName() {
        return REQUEST_PARAM_NAME;
    }

    @Override
    public boolean hasValidToken(final HttpServletRequest request) {
        final HttpSession session = getSession(request);
        if (session == null) {
            return false;
        }

        final String requestToken = request.getParameter(getXsrfTokenName());
        final String sessionToken = (String) session.getAttribute(TOKEN_SESSION_KEY);

        // null is never a valid token so both the requestToken and sessionToken
        // cannot be null
        return requestToken != null && sessionToken != null
                && ConstantTimeComparison.isEqual(requestToken, sessionToken);
    }

    protected String createToken() {
        return secureTokenGenerator.generateToken();
    }

    /**
     * In the case of an authenticated user, we create a session if one doesn't exist. Some companies use sessionless
     * authentication methods like Basic Auth through a reverse proxy. This would result in "XSRF Token Missing" pages
     * for their users. The session wouldn't have been created yet, so we wouldn't be able to add our XSRF token to the
     * session without first creating the session ourselves.
     */
    protected HttpSession getSession(final HttpServletRequest request) {
        final boolean createSessionIfMissing = authenticationContext.isAuthenticated();
        return request.getSession(createSessionIfMissing);
    }
}
