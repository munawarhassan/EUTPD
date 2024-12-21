package com.pmi.tpd.core.security.spring;

import java.util.Map;

import org.springframework.security.web.authentication.rememberme.PersistentRememberMeToken;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;

/**
 * <p>
 * LocalizedTokenRepository interface.
 * </p>
 *
 * @author Christophe Friederich
 * @since 1.0
 */
public interface ILocalizedTokenRepository extends PersistentTokenRepository {

    /**
     * <p>
     * createNewToken.
     * </p>
     *
     * @param token
     *            a {@link org.springframework.security.web.authentication.rememberme.PersistentRememberMeToken} object.
     * @param localisationInfos
     *            a {@link java.util.Map} object.
     */
    void createNewToken(PersistentRememberMeToken token, Map<String, Object> localisationInfos);
}
