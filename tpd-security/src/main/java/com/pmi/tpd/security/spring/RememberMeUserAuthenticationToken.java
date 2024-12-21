package com.pmi.tpd.security.spring;

import static com.pmi.tpd.api.util.Assert.checkNotNull;

import org.springframework.security.core.userdetails.UserDetails;

import com.pmi.tpd.api.user.IUser;

/**
 * @author Christophe Friederich
 * @since 2.0
 */
public final class RememberMeUserAuthenticationToken extends UserAuthenticationToken {

    /**
     *
     */
    private static final long serialVersionUID = -5456827982713653019L;

    /** */
    private final String key;

    /**
     * @param user
     * @param userDetails
     * @param key
     *            to identify if this object made by an authorised client
     * @return
     */
    public static RememberMeUserAuthenticationToken forUser(final IUser user,
        final UserDetails userDetails,
        final String key) {
        return new RememberMeUserAuthenticationToken(
                UserAuthenticationToken.builder().user(user).userDetails(userDetails), key);
    }

    /**
     * @param builder
     * @param key
     *            to identify if this object made by an authorised client
     */
    private RememberMeUserAuthenticationToken(final Builder builder, final String key) {
        super(builder);
        this.key = checkNotNull(key, "key");
    }

    /**
     * Gets hashCode of above given key.
     *
     * @return Returns a {@link String} representing the hashCode of above given key.
     */
    public int getKeyHash() {
        return key.hashCode();
    }
}
