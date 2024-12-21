package com.pmi.tpd.security.random;

import javax.annotation.Nonnull;

/**
 * Generator for randomly generated {@link String}s which can be used for cryptographically secure tokens.
 * <p/>
 * These tokens might be used for cross-site request forgery (XSRF) prevention, authentication representation (eg.
 * forgot password tokens, remember-me tokens).
 * <p/>
 * Implementations are required to be thread-safe.
 * 
 * @author Christophe Friederich
 * @since 1.3
 */
public interface ISecureTokenGenerator {

    /**
     * Generates an randomly generated token that is suitable for use as an authentication token. The token should be
     * treated as an opaque String and not be parsed or interpreted by the client. The returned {@link String} will not
     * be longer than 255 characters.
     *
     * @return a randomly generated opaque {@link String}.
     * @see DefaultSecureTokenGenerator#generateToken()
     */
    @Nonnull
    String generateToken();
}
