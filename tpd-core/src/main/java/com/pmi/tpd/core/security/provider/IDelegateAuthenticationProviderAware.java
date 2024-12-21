package com.pmi.tpd.core.security.provider;

/**
 * @author Christophe Friederich
 * @since 2.0
 */
public interface IDelegateAuthenticationProviderAware {

    /**
     * @param provider
     *            the delegate authentication provider.
     */
    void setDelegate(IAuthenticationProvider provider);
}
