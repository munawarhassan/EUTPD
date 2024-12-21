package com.pmi.tpd.core.security.web.auth;

import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;

/**
 * @author Christophe Friederich
 * @since 1.3
 */
public interface XsrfTokenGenerator {

    @Nullable
    String generateToken(HttpServletRequest request);

    String getXsrfTokenName();

    boolean hasValidToken(HttpServletRequest request);

}
