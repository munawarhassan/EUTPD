package com.pmi.tpd.web.security.jwt;

import com.pmi.tpd.api.config.annotation.ConfigurationProperties;

/**
 * @author devacfr
 * @since 1.0
 */
@ConfigurationProperties("security.authentication.jwt")
public class JwtConfiguration {

    /** */
    private String secret;

    /** */
    private int tokenValidityInSeconds;

    /** */
    private int tokenValidityInSecondsForRememberMe;

    /**
     * @return the secret
     */
    public String getSecret() {
        return secret;
    }

    /**
     * @param secret
     *            the secret to set
     */
    public void setSecret(final String secret) {
        this.secret = secret;
    }

    /**
     * @return the tokenValidityInSeconds
     */
    public int getTokenValidityInSeconds() {
        return tokenValidityInSeconds;
    }

    /**
     * @param tokenValidityInSeconds
     *            the tokenValidityInSeconds to set
     */
    public void setTokenValidityInSeconds(final int tokenValidityInSeconds) {
        this.tokenValidityInSeconds = tokenValidityInSeconds;
    }

    /**
     * @return the tokenValidityInSecondsForRememberMe
     */
    public int getTokenValidityInSecondsForRememberMe() {
        return tokenValidityInSecondsForRememberMe;
    }

    /**
     * @param tokenValidityInSecondsForRememberMe
     *            the tokenValidityInSecondsForRememberMe to set
     */
    public void setTokenValidityInSecondsForRememberMe(final int tokenValidityInSecondsForRememberMe) {
        this.tokenValidityInSecondsForRememberMe = tokenValidityInSecondsForRememberMe;
    }

}
