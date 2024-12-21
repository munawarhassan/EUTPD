package com.pmi.tpd.security;

import java.util.Optional;

import com.pmi.tpd.api.user.IUser;
import com.pmi.tpd.security.spring.UserAuthenticationToken;

/**
 * Obtains the current user logged in application.
 *
 * @author Christophe Friederich
 * @since 1.3
 */
public interface IAuthenticationContext {

    /**
     * @return
     * @since 2.0
     */
    Optional<UserAuthenticationToken> getCurrentToken();

    /**
     * @return {@code true} if the current request is authenticated
     */
    boolean isAuthenticated();

    /**
     * @return the current user or {@code null} if anonymous
     */
    Optional<IUser> getCurrentUser();

}
