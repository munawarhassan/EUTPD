package com.pmi.tpd.core.security.provider.ldap;

import javax.annotation.Nonnull;

import com.pmi.tpd.core.security.configuration.IAuthenticationProperties;

/**
 * @author Christophe Friederich
 * @since 2.0
 */
public interface IAuthenticationCheckConnection {

    /**
     * Checks Authentication connection.
     *
     * @param config
     *            Authentication configuration.
     */
    void checkConnection(@Nonnull IAuthenticationProperties config);

}
